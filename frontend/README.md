# Frontend

Modul pro frontend projektu FREING

<hr>

## Moduly
Členění FE jde dle service ke které patří, tedy:
- invoice-service -> invoice
- customer-service -> customer
- payment-service -> payment (zatím není implementováno)

<hr>

### OpenAPI automatické generování tříd
Spuštění generování proběhne jako postinstall akce při nasazení v kontejneru, pro lokální vývoj je potřeba spustit ručně jako postinstal akci.  
Pro vygenerování tříd z OpenAPI pro využití při vývoji FE je nutné, aby SVCs běžely v dockeru. Poté je možné v package.json spustit postinstall akci ručně.

<hr>

## Changelog
- version **0.0.1** (2025-10-06)
  - Přidána základní frontend struktura
  - Přidány základní komponenty a stránky
  - Přidána podpora pro React Router
  - Automatické generování OpenAPI dokumentace, postará se o přidání DTO a requestů, které jso udefinovány v SVC (momenátlně pouze invoice service!)
    - Při npm install je třeba sldovat konzoli (a je nutné mít zapnutou SVC, aby se to vygenerovalo)
- version **0.0.2** (2025-10-11)
  - Script update, launch settings fix, env var fix.
- version **0.0.3** (2025-10-11)
  - Přidán exchange rate přepočet při zadávání faktury. 
- version **0.0.4** (2025-10-28)
  - Oprava chyb v generování OpenAPI klienta
  - Přidána podpora pro více služeb v generátoru OpenAPI klienta
- version **0.0.5** (2025-11-07)
  - Aktualizace OpenAPI klienta pro nové změny ve službě Invoice Service a Customer Service
  - Oprava chyb v některých komponentách a stránkách
  - Vylepšení generoování z OpenAPI
