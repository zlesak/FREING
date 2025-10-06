import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { HeaderComponent } from './components/header/header.components';
import { InvoicesPageComponent } from './features/invoices/view/invoices-page.component';
import { CustomersPageComponent } from './features/customers/view/customers-page.component';
import { PaymentsPageComponent } from './features/payments/view/payments-page.component';
import {HomePageComponent} from './features/home/view/home-page.component';
import { InvoiceCreateComponent } from './features/invoices/components/invoice-create/invoice-create.component';

@NgModule({
  declarations: [
    App,
    HeaderComponent,
    HomePageComponent,
    InvoicesPageComponent,
    CustomersPageComponent,
    PaymentsPageComponent,
    InvoiceCreateComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
