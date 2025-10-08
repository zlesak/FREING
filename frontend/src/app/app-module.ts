import { NgModule, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { KeycloakService } from './keycloak.service';

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
    provideBrowserGlobalErrorListeners(),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: (keycloakService: KeycloakService) => () => keycloakService.init(),
      deps: [KeycloakService],
      multi: true
    }
  ],
  bootstrap: [App]
})
export class AppModule { }
