const fs = require('fs');
const crypto = require('crypto');
const { execSync } = require('child_process');

const OPENAPI_URL = 'http://localhost:8082/v3/api-docs';
const OPENAPI_FILE = 'openapi.json';
const HASH_FILE = '.openapi.hash';
const GENERATED_INDEX = 'src/app/api/generated/index.ts';

function calculateHash(content) {
  return crypto.createHash('sha256').update(content).digest('hex');
}

function downloadOpenAPI() {
  try {
    console.log('Downloading OpenAPI specification...');
    execSync(`curl -s ${OPENAPI_URL} -o ${OPENAPI_FILE}`, { stdio: 'inherit' });
    return true;
  } catch (error) {
    console.warn('Failed to download OpenAPI specification. Backend might not be running.');
    return false;
  }
}

function getCurrentHash() {
  if (!fs.existsSync(OPENAPI_FILE)) {
    return null;
  }
  const content = fs.readFileSync(OPENAPI_FILE, 'utf8');
  return calculateHash(content);
}

function getStoredHash() {
  if (!fs.existsSync(HASH_FILE)) {
    return null;
  }
  return fs.readFileSync(HASH_FILE, 'utf8').trim();
}

function storeHash(hash) {
  fs.writeFileSync(HASH_FILE, hash);
}

function generatedClientExists() {
  return fs.existsSync(GENERATED_INDEX);
}

function generateClient() {
  try {
    console.log('Generating OpenAPI client...');
    execSync('npx openapi --input openapi.json --output src/app/api/generated --client fetch --useOptions --useUnionTypes', { stdio: 'inherit' });
    return true;
  } catch (error) {
    console.error('Failed to generate OpenAPI client:', error.message);
    return false;
  }
}

function main() {
  // Check if generated client exists
  if (!generatedClientExists()) {
    console.log('OpenAPI client not found. Generating...');
    if (downloadOpenAPI()) {
      const currentHash = getCurrentHash();
      if (currentHash && generateClient()) {
        storeHash(currentHash);
        console.log('OpenAPI client generated successfully.');
      }
    }
    return;
  }

  // Download current OpenAPI spec
  if (!downloadOpenAPI()) {
    console.log('OpenAPI client already present, backend not available for update check.');
    return;
  }

  const currentHash = getCurrentHash();
  const storedHash = getStoredHash();

  if (!currentHash) {
    console.error('Failed to calculate current OpenAPI hash.');
    return;
  }

  if (currentHash === storedHash) {
    console.log('OpenAPI client is up to date.');
    return;
  }

  console.log('OpenAPI specification changed. Regenerating client...');
  if (generateClient()) {
    storeHash(currentHash);
    console.log('OpenAPI client updated successfully.');
  }
}

main();
