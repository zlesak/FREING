# FREING Docker — Keycloak a API gateway

Tento README popisuje, jak rychle spustit Keycloak a API gateway pomocí Docker Compose pro lokální vývoj a testování (používá konfiguraci v tomto adresáři).

Důležitá poznámka — /etc/hosts
- Aby fungovaly domény používané v konfiguraci (např. `auth.test` a `freing.test`), je nutné přidat záznamy do vašeho `/etc/hosts`, například:

  sudo nano /etc/hosts
  # přidejte tento řádek:
  127.0.0.1 auth.test freing.test

  Nebo jednorázově z příkazové řádky (macOS / Linux):

  sudo sh -c 'echo "127.0.0.1 auth.test freing.test" >> /etc/hosts'

- Po této úpravě budou požadavky na `http://auth.test` a `http://freing.test` směřovat na váš lokální stroj (na gateway běžící v Dockeru, která naslouchá na portu 80).

Předpoklady
- Docker a Docker Compose (moderní Docker používá příkaz `docker compose`).
- Volitelně: frontend (Angular) spuštěný lokálně na portu 4200 pokud chcete, aby `freing.test` proxyoval na lokální frontend (viz níže).
- Volitelně: Gradle/Java v hostu pokud chcete, aby backend kontejnery spouštěly `./gradlew bootRun` z hostovaného kódu (soubory jsou v `../backend/...`).

Co tento compose dělá (stručně)
- `gateway` — Nginx naslouchá na portu 80 a podle server_name přeposílá:
  - `auth.test` -> Keycloak (container `keycloak_web:8100`)
  - `freing.test` -> frontend (upstream `frontend` -> očekává buď container `frontend:4200` nebo lokální `host.docker.internal:4200` jako backup)
- `keycloak_web` — Keycloak (image `quay.io/keycloak/keycloak:23.0.2`) s importem realm (`/keycloak/freing-realm.json`), vystavený interně na portu 8100 a mapovaný i na host port 8100.
- `keycloak_db` — Postgres databáze pro Keycloak (vytrvalé volume `postgres_data`).
- Dále jsou v compose služby backendu (`customer_service`, `invoice_service`, `payment_service`, `rendering_service`) — ty používají `openjdk` image a spouštějí `./gradlew bootRun` v mountnutých adresářích z hostu.

Rychlý start (gateway + keycloak)
1. Ověřte, že máte v `/etc/hosts` přidané `auth.test` a `freing.test` (viz výše).
2. Otevřete terminál v tomto adresáři (`freing_docker`) a spusťte:

```bash
cd /path/to/FREING/freing_docker
# doporučené: start databáze, keycloak a gateway
docker compose up -d keycloak_db keycloak_web gateway
```

3. Zkontrolujte logy (např. Keycloak):

```bash
docker compose logs -f keycloak_web
# nebo gateway
docker compose logs -f gateway
```

4. Otevřete v prohlížeči: `http://auth.test` — mělo by vás přesměrovat do Keycloak (admin konzole). Admin přihlašovací údaje jsou nastaveny v compose (pro dev):

- Uživatelské jméno: `admin`
- Heslo: `password`

Poznámka: compose používá `start-dev --import-realm`, takže realm `freing` bude naimportován z `freing_docker/keycloak/freing-realm.json` při prvním startu.

Frontend a `freing.test`
- Nginx nakonfigurovaný v `api_gateway/nginx.conf` směruje `freing.test` na upstream `frontend`.
- Upstream `frontend` je složen tak, že nejprve hledá kontejner `frontend:4200` (pokud ho spustíte v Dockeru), a jako fallback používá `host.docker.internal:4200` (pokud spustíte Angular frontend lokálně na hostu).
- Pokud chceš použít lokální frontend (doporučené pro rychlý vývoj):

```bash
# ve druhém terminálu (project root)
cd frontend
npm install   # pokud ještě není nainstalováno
npm start     # spustí Angular dev server na http://localhost:4200
```

Pak otevři `http://freing.test` — gateway přepošle požadavky na lokální frontend.

Backend služby (volitelné)
- V compose jsou také služby, které mountují zdrojový kód z `../backend/*` a spouští `./gradlew bootRun` uvnitř kontejneru. To umožňuje spouštět backend přímo v kontejnerech, ale vyžaduje, aby v těch adresářích byl `gradlew` a všechny závislosti.
- Alternativně můžeš backendy spouštět lokálně přes Gradle (doporučeno při aktivním vývoji):

```bash
cd backend/invoice-service
./gradlew bootRun
# nebo Windows: gradlew.bat bootRun
```

Ukončení a reset
- Zastavení všech služeb a odstranění kontejnerů (data Postgresu zůstane):

```bash
docker compose down
```

- Odstranění i volume (vymaže Keycloak DB, použít pokud chceš čistý start):

```bash
docker compose down -v
```

Rychlá diagnostika
- Pokud něco nefunguje:
  - Zkontroluj, že port 80 na hostu nebyl obsazen jinou službou.
  - Přesvědč se, že `/etc/hosts` obsahuje `auth.test` a `freing.test` ukazující na `127.0.0.1`.
  - Podívej se do logů: `docker compose logs -f keycloak_web gateway`.

Bezpečnost / poznámky pro produkci
- Konfigurace v tomto repozitáři je určena pro lokální vývoj (dev) — používá jednoduchá hesla, `start-dev` mód Keycloaku a nebezpečné nastavení hostnames/HTTP.
- Nepoužívej tyto nastavení v produkci bez adekvátních změn (HTTPS, pevná hesla, bezpečné DB, zálohy atd.).

Kontakt / další kroky
- Pokud chceš, mohu přidat: skript, který automaticky přidá položky do `/etc/hosts` (vyžaduje sudo), nebo `Makefile`/skript pro pohodlné spuštění/stop.

---
Soubor `docker-compose.yml` a `api_gateway/nginx.conf` v tomto adresáři definují nastavení — uprav je, pokud potřebuješ jiné hostnames/porty.

## Testovací účty

Pro rychlé testování přihlášení a rolí v Keycloaku jsou dostupné tyto účty:

- Uživatelské jméno: `alice.accountant`  
  Heslo: `XK3mMLHm7nH5mygTW`  
  Role: `accountant`

- Uživatelské jméno: `bob.manager`  
  Heslo: `1p470ZIoQPNGyJioZ`  
  Role: `manager`

- Uživatelské jméno: `john.customer`  
  Heslo: `9a1onhpI8rpu3WW73`  
  Role: `customer`

Poznámka: Tyto účty jsou určeny pouze pro lokální vývoj. Pokud Keycloak importuje realm z `keycloak/freing-realm.json`, účty by měly být dostupné po importu. Pokud nejsou, přidejte nebo upravte uživatele v Keycloak admin konzoli (`http://auth.test`).
