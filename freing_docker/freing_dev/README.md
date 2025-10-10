# FREING Docker - dev

## 1. přidání testovacích domén do /etc/hosts
- Aby fungovaly domény používané v konfiguraci (např. `auth.test`, `freing.test` a `service.freing.test`), je nutné přidat záznamy do vašeho `/etc/hosts`, například:

  `sudo nano /etc/hosts`
  přidejte tento řádek: 127.0.0.1 auth.test freing.test service.freing.test

## 2. spuštění `docker compose up -d`

```bash
cd /path/to/FREING/freing_docker
docker compose up -d
```

## Přístup do keycloak admin konzole
- Otevřete v prohlížeči: `http://auth.test/admin/` — mělo by vás přesměrovat do Keycloak (admin konzole). Admin přihlašovací údaje jsou nastaveny v compose (pro dev):

- Uživatelské jméno: `admin`
- Heslo: `password`

Poznámka: compose používá `start-dev --import-realm`, takže realm `freing` bude naimportován z `freing_docker/keycloak/freing-realm.json` při prvním startu.

## Přístup do aplikace FREING

- Keycloak (autentizace, autorizace):
  - `http://auth.test`
- Frontend aplikace:
  - `http://freing.test`
- Backend services:
  - `http://service.freing.test`
    - Customer Service
      - `http://service.freing.test/customer`
    - Invoice Service
      - `http://service.freing.test/invoice`
    - Payment Service
      - `http://service.freing.test/payment`
    - Rendering Service
      - `http://service.freing.test/rendering`


## Testovací účty

Pro rychlé testování přihlášení a rolí v Keycloaku jsou dostupné tyto účty:

- Uživatelské jméno: `alice`
  - Heslo: `XK3mMLHm7nH5mygTW`
  - Role: `accountant`

- Uživatelské jméno: `bob`
  - Heslo: `1p470ZIoQPNGyJioZ`
  - Role: `manager`

- Uživatelské jméno: `john`
  - Heslo: `9a1onhpI8rpu3WW73`
  - Role: `customer`

Poznámka: Tyto účty jsou určeny pouze pro lokální vývoj. Pokud Keycloak importuje realm z `keycloak/freing-realm.json`, účty by měly být dostupné po importu. Pokud nejsou, přidejte nebo upravte uživatele v Keycloak admin konzoli (`http://auth.test`).
