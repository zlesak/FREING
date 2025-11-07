# Payment service

Customer se svým účtem se přihlásí, do portálu bude mít přístup jen role CUSTOMER.

Uvidí dashboard (Payments tab), kde bude mít faktury, které mu patří.

Na dashboardu uvidí název, referenční číslo, stav a částku faktury.  
Uvidí zde ale jen a pouze faktury, které jsou ve stavech SENT, PENDING, PAID, CANCELLED, OVERDUE a PAID_OVERDUE (tedy ne DRAFT).

Po rozkliknutí bude mít možnost vidět detail faktury z dat, která jsou uložená, tzn. všechny známé údaje. Také si bude moci stáhnout generovanou PDF fakturu.

Endpoint pro generovanou fakturu na `/api/payments/invoice/ID/render`.

Záleží, jestli chceme fakturu pak vykreslovat, nebo jestli chceme, aby se rovnou stáhla. To je na DEV.

## Stavy faktury a jejich přechody

- **DRAFT** – faktura je ve stavu konceptu, není viditelná pro zákazníka.
- **SENT** – faktura byla odeslána zákazníkovi, čeká na to, až si ji zobrazí. Po SENT se nebude dát upravovat v invoice SVC.
- **PENDING** – faktura byla zobrazena zákazníkem, čeká na platbu.
- **PAID** – faktura byla zaplacena včas.
- **CANCELLED** – faktura byla zrušena, může být pouze zobrazena. Přepnout do tohoto stavu může jen ACCOUNTANT v invoice SVC.
- **OVERDUE** – faktura je po splatnosti, čeká na platbu.
- **PAID_OVERDUE** – faktura byla zaplacena po splatnosti.

Pokud je faktura SENT a uživatel si ji zobrazí, je nutno přepnout stav na PENDING.  
Pokud je faktura PENDING a uživatel ji zaplatí, stav se přepne na PAID.  
Pokud je faktura OVERDUE a uživatel ji zaplatí, stav se přepne na PAID_OVERDUE.  
Pokud je faktura PAID, PAID_OVERDUE nebo CANCELLED, stav zůstává stejný při jakékoliv další akci uživatele, toto jsou finální stavy.

Na straně invoice SVC je nutno dělat nějakou cron akci, která bude kontrolovat všechny faktury ve stavu SENT a PENDING a pokud je jejich splatnost v minulosti, přepne je do stavu OVERDUE.

Propojení na SB PayPal, aby bylo možné fakturu zaplatit.  
Jakmile bude faktura zaplacena, je nutno to reflektovat ve stavu faktury dle výše uvedených pravidel.

Je nutno nastavit přechody stavů, viz `StateDiagram.puml`.
![StateDiagram-0.png](StateDiagram-0.png)