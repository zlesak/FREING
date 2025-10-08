// frontend/src/app/keycloak.service.ts
import { Injectable } from '@angular/core';
import Keycloak, { KeycloakInstance, KeycloakInitOptions } from 'keycloak-js';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private keycloakAuth!: KeycloakInstance;

  public async init(): Promise<void> {
    this.keycloakAuth = new Keycloak({
      url: 'http://auth.test/',
      realm: 'freing',
      clientId: 'frontend',
    });

    const initOptions: KeycloakInitOptions = {
      onLoad: 'login-required',
      checkLoginIframe: false,
      enableLogging: true,
    };

    return new Promise((resolve, reject) => {
      this.keycloakAuth.init(initOptions).then((authenticated) => {
        if (authenticated) {
          resolve();
        } else {
          reject('Neautentizováno');
        }
      }).catch((error) => reject(error));
    });
  }

  public isLoggedIn(): boolean {
    return !!this.keycloakAuth && !!this.keycloakAuth.token;
  }

  public getToken(): string | undefined {
    return this.keycloakAuth?.token;
  }

  public login(): void {
    this.keycloakAuth.login();
  }

  public logout(): void {
    this.keycloakAuth.logout();
  }
}
