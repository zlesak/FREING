# FREING Docker - dev

## Lokální vývojové prostředí pro FREING pomocí Docker Compose
Tento projekt obsahuje `docker-compose.yml` pro spuštění lokálního vývojového prostředí FREING pomocí Docker Compose.  
Služby:  
- Keycloak (pro autentizaci a autorizaci)
- PostgreSQL databáze (jedna pro každou mikroservisu)
- PgAdmin pro správu databází
- FREING mikroservisy (Customer Service, Invoice Service, Payment Service, Rendering Service)
- Frontend aplikaci

## Postup pro spuštění
### 1. Přidání testovacích domén do /etc/hosts (unix-based systémy)
Aby fungovaly domény používané v konfiguraci (např. `auth.freing.test`, `freing.test`, `service.freing.test` a `pgadmin.freing.test`), je nutné přidat záznamy do `/etc/hosts`.  
Otevřte `sudo nano /etc/hosts` a přidejte tento řádek: `127.0.0.1 auth.freing.test rabbitmq.freing.test freing.test service.freing.test pgadmin.freing.test`  

nebo použijte příkaz:
```bash
 echo "127.0.0.1 auth.freing.test rabbitmq.freing.test freing.test service.freing.test pgadmin.freing.test" >> /etc/hosts
```
Pro Windows upravte `C:\Windows\System32\drivers\etc\hosts` obdobně.


### 2. Spuštění Docker Compose

V IDE nebo terminálu přejděte do složky `freing_docker` a spusťte Docker Compose a spusťte `docker compose up -d` (nebo skrzer IDE).

```bash
cd /{YOUR_PATH_TO_THE_PROJECT_DIRECTORY}/FREING/freing_docker
docker compose up -d
```

## Přístup do keycloak admin konzole
- Otevřete v prohlížeči: `http://auth.freing.test/admin/` — mělo by vás přesměrodit do Keycloak (admin konzole). Admin přihlašovací údaje jsou nastaveny v compose (pro dev):

- Uživatelské jméno: `admin`
- Heslo: `password`

Poznámka: compose používá `start-dev --import-realm`, takže realm `freing` bude naimportován z `freing_docker/keycloak/freing-realm.json` při prvním startu.

## Přístup do aplikace FREING

- Keycloak (autentizace, autorizace):
  - `http://auth.freing.test`
- RabbitMQ:
  - `http://rabbitmq.freing.test` 
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

Poznámka: Tyto účty jsou určeny pouze pro lokální vývoj. Pokud Keycloak importuje realm z `keycloak/freing-realm.json`, účty by měly být dostupné po importu. Pokud nejsou, přidejte nebo upravte uživatele v Keycloak admin konzoli (`http://auth.freing.test`).


## PGAdmin
V případě potřeby je možno se připojit k databázím pomocí těchto údajů:
- Přístup do PGAdmin: `http://pgadmin.freing.test`
- Uživatelské jméno: `admin@example.com`
- Heslo: `admin`

## RabbitMQ
Přístup k RabbitMQ: `http://rabbitmq.freing.test`
- Uživatelské jméno: `rabbitmq`
- Heslo: `rabbitmq_pass`