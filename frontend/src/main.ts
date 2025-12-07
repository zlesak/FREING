// main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { importProvidersFrom, APP_INITIALIZER, LOCALE_ID } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { registerLocaleData } from '@angular/common';
import localeCs from '@angular/common/locales/cs';

import { App } from './app/app';
import { routes } from './app/app-routing-module';
import { KeycloakService } from './app/security/keycloak.service';
import { AuthInterceptor } from './app/security/AuthInterceptor';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

registerLocaleData(localeCs);

bootstrapApplication(App, {
  providers: [
    provideRouter(routes),
    importProvidersFrom(FormsModule, ReactiveFormsModule),
    provideHttpClient(withInterceptorsFromDi()),
    KeycloakService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    {
      provide: APP_INITIALIZER,
      useFactory: (kc: KeycloakService) => () => kc.init(),
      deps: [KeycloakService],
      multi: true,
    },
    { provide: LOCALE_ID, useValue: 'cs-CZ' },
  ],
}).catch(err => console.error(err));
