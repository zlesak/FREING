const fs = require('fs');
const crypto = require('crypto');
const { execSync } = require('child_process');

const gatewayHost = process.env.GATEWAY_HOST || 'service.freing.test';
const servicePorts = { invoice: 8082, customer: 8081, payment: 8083, rendering: 8084 };

const services = Object.keys(servicePorts).map(name => {
  const envVar = process.env[`OPENAPI_${name.toUpperCase()}_URL`];
  const port = servicePorts[name];
  const candidates = [
    envVar,
    `http://${gatewayHost}/${name}/v3/api-docs`,
    `http://service-${name}:${port}/v3/api-docs`,
    `http://localhost:${port}/v3/api-docs`
  ].filter(Boolean);
  return { name, candidates };
});

function sha(content) { return crypto.createHash('sha256').update(content).digest('hex'); }

function download(service) {
  for (const url of service.candidates) {
    const targetFile = `openapi-${service.name}.json`;
    try {
      console.log(`[${service.name}] Trying ${url}`);
      execSync(`curl -s --fail ${url} -o ${targetFile}`);
      if (fs.existsSync(targetFile) && fs.statSync(targetFile).size > 0) {
        service.selectedUrl = url;
        console.log(`[${service.name}] Downloaded spec from ${url}`);
        return true;
      }
    } catch (e) {
      console.warn(`[${service.name}] Candidate failed: ${url} (${e.message})`);
    }
  }
  console.error(`[${service.name}] All candidates failed.`);
  return false;
}

function needsGeneration(service) {
  const specFile = `openapi-${service.name}.json`;
  const hashFile = `.openapi-${service.name}.hash`;
  const outIndex = `src/app/api/generated/${service.name}/index.ts`;
  if (!fs.existsSync(specFile)) return true;
  if (!fs.existsSync(outIndex)) return true;
  const content = fs.readFileSync(specFile, 'utf8');
  if (!content.trim().startsWith('{')) {
    console.warn(`[${service.name}] Spec file does not look like JSON, forcing regeneration.`);
    return true;
  }
  const currentHash = sha(content);
  const storedHash = fs.existsSync(hashFile) ? fs.readFileSync(hashFile, 'utf8').trim() : null;
  return currentHash !== storedHash;
}

function generate(service) {
  const specFile = `openapi-${service.name}.json`;
  const outDir = `src/app/api/generated/${service.name}`;
  console.log(`[${service.name}] Generating client...`);
  try {
    if (fs.existsSync(outDir)) {
      fs.rmSync(outDir, { recursive: true, force: true });
    }
    execSync(`npx openapi --input ${specFile} --output ${outDir} --client angular --useOptions --useUnionTypes`, { stdio: 'inherit' });
    const hash = sha(fs.readFileSync(specFile, 'utf8'));
    fs.writeFileSync(`.openapi-${service.name}.hash`, hash);
    console.log(`[${service.name}] Client generated.`);
  } catch (e) {
    console.error(`[${service.name}] Generation failed: ${e.message}`);
  }
}

function writeAggregator() {
  const lines = services.map(s => `export * as ${capitalize(s.name)}Api from './${s.name}';`);
  const content = '// Auto-generated aggregator index for all OpenAPI clients.\n' + lines.join('\n') + '\n';
  const target = 'src/app/api/generated/index.ts';
  fs.mkdirSync('src/app/api/generated', { recursive: true });
  fs.writeFileSync(target, content);
  console.log('Aggregator index written to', target);
}

function capitalize(str){return str.charAt(0).toUpperCase()+str.slice(1);}

function main() {
  let anyGenerated = false;
  for (const s of services) {
    const downloaded = download(s);
    if (!downloaded) {
      console.log(`[${s.name}] Skip (spec not downloaded).`);
      continue;
    }
    if (needsGeneration(s)) {
      generate(s);
      anyGenerated = true;
    } else {
      console.log(`[${s.name}] Up to date.`);
    }
  }
  writeAggregator();
  if (!anyGenerated) {
    console.log('All OpenAPI clients are up to date.');
  }
}

main();
