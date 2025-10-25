import {NgModule, provideBrowserGlobalErrorListeners, APP_INITIALIZER, LOCALE_ID} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { KeycloakService } from './keycloak.service';
import {HeaderComponent} from './components/header/header.components';
import {HomePageComponent} from './features/home/view/home-page.component';
import {InvoicesPageComponent} from './features/invoices/view/invoices-page.component';
import {CustomersPageComponent} from './features/customers/view/customers-page.component';
import {PaymentsPageComponent} from './features/payments/view/payments-page.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {registerLocaleData} from '@angular/common';
import localeCs from '@angular/common/locales/cs';

registerLocaleData(localeCs);
@NgModule({
  declarations: [
    App,
    HeaderComponent,
    HomePageComponent,
    CustomersPageComponent,
    PaymentsPageComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    InvoicesPageComponent,
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: (keycloakService: KeycloakService) => () => keycloakService.init(),
      deps: [KeycloakService],
      multi: true
    },
    { provide: LOCALE_ID, useValue: 'cs-CZ' }
  ],
  bootstrap: [App]
})
export class AppModule { }
