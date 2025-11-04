import { NgModule, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { KeycloakService } from './keycloak.service';
import { OpenAPI as InvoiceOpenAPI } from './api/generated/invoice/core/OpenAPI';
import { OpenAPI as CustomerOpenAPI } from './api/generated/customer/core/OpenAPI';
import { OpenAPI as PaymentOpenAPI } from './api/generated/payment/core/OpenAPI';
import { OpenAPI as RenderingOpenAPI } from './api/generated/rendering/core/OpenAPI';
import {HeaderComponent} from './components/header/header.components';
import {HomePageComponent} from './features/home/view/home-page.component';
import {InvoicesPageComponent} from './features/invoices/view/invoices-page.component';
import {CustomersPageComponent} from './features/customers/view/customers-page.component';
import {PaymentsPageComponent} from './features/payments/view/payments-page.component';
import {InvoiceCreateComponent} from './features/invoices/components/invoice-create/invoice-create.component';
import {CustomerCreateComponent} from './features/customers/components/customer-create/customer-create.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@NgModule({
  declarations: [
    App,
    HeaderComponent,
    HomePageComponent,
    InvoicesPageComponent,
    CustomersPageComponent,
    PaymentsPageComponent,
    InvoiceCreateComponent,
    CustomerCreateComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: (keycloakService: KeycloakService) => async () => {
        await keycloakService.init();
        // Set token resolver for OpenAPI generated clients
        const tokenResolver = async () => keycloakService.getToken() || '';
        InvoiceOpenAPI.TOKEN = tokenResolver;
        CustomerOpenAPI.TOKEN = tokenResolver;
        PaymentOpenAPI.TOKEN = tokenResolver;
        RenderingOpenAPI.TOKEN = tokenResolver;
      },
      deps: [KeycloakService],
      multi: true
    }
  ],
  bootstrap: [App]
})
export class AppModule { }
