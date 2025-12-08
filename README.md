# FREING
![Logo](./docs/logo.png "Logo")  
Zápočtový projekt týmu Endor předmětu MOIS ZS AR 2025/26

## Členové týmu
- j.zlesak (Team leader)
- j.staša (Configuration manager)
- j.fogl (BackEnd develeper)
- a.zamastil (FrontEnd developer)

## Členění projektu
`FREING` - Kořenový adresář projektu  
├── `backend` - Zdrojové kódy backendu  
├── `diagrams` - Diagramy  
├── `docs` - Dokumentace  
├── `freing_docker` - Soubory pro Docker a Docker Compose   
├── `frontend` - Zdrojové kódy frontendu  
└── `screenshots` - Snímky obrazovky aplikace  

### Pohledy na aplikaci  

#### Pohledy účetního  
![Dashboards](./screenshots/accountant_main_page.png "Main page")  
![Add customer](./screenshots/accountant_add_customer.png "Add customer")  
![Add invoice](./screenshots/accountant_add_invoice.png "Add invoice")  
![Invoice detail](./screenshots/accountant_invoice_detail.png "Invoice detail")  
![PDF View](./screenshots/accountant_pdf_view.png "PDF View")  

#### Pohledy zákazníka  
![Payment page step one](./screenshots/customer_payment_step_one.png "Payment page step one")  
![Payment page step two](./screenshots/customer_payment_step_two.png "Payment page step two")  
![Payed invoice page customer](./screenshots/customer_paid_invoice.png "Payed invoice page customer")  

#### Pohledy manažera  
![Manager supplier page](./screenshots/manager_supplier_page.png "Manager supplier page")  
![Manager new supplier](./screenshots/manager_new_supplier.png "Manager new suplier page")  
![Manager report page](./screenshots/manager_report_page.png "Manager report page")  

#### Generované PDF  
![Generated invoice PDF](./screenshots/invoice_pdf_has_data_invoice_f.png "Generated invoice PDF")  
V /docs/ je také k dispozici vygenerovaný facturx dokument přikládaný ke každé faktuře jako digitální příloha.  
  
![Generated report PDF 1](./screenshots/report_pdf_1.png "Generated report 1 PDF")  
![Generated report PDF 2](./screenshots/report_pdf_2.png "Generated report 2 PDF")  
I pro reporty se přikládají data v xml podobě ze kterých byl report vygenerován.

### Základní diagramy  

#### Use case diagram  

![Use Case](./diagrams/FREING_USE_CASE_DIAGRAM.png "Use Case")

#### Big picture

![Big picture](./diagrams/FREING_BIG_PIC_DIAGRAM.png "Use Case")

#### Data diagram

![All service data diagram](./diagrams/all_service_data_diagram.png "All service data diagram")

#### Business context

![Bussines context](./diagrams/FREING_BUSINESS_CONTEXT_DIAGRAM.png "Bussines context")

#### Technologie využité v projektu

![Technologie](./diagrams/MOIS2.drawio.png "Technologie")

#### Infrastruktura projektu

![Infrastruktura](./diagrams/FREING_INFRASTRUCTURE_DIAGRAM.png "Infrastruktura")

### Diagram stavů faktur

![Stavový diagram](./diagrams/FREING_STATE_DIAGRAM.png "Stavový diagram")