import { Injectable } from '@angular/core';
import Keycloak, { KeycloakInstance } from 'keycloak-js';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private keycloakAuth!: KeycloakInstance;

  public async init(): Promise<void> {
    this.keycloakAuth = new Keycloak({
      url: 'http://localhost:8080/', // upravte dle potřeby
      realm: 'freing',
      clientId: 'frontend',
    });
    return new Promise((resolve, reject) => {
      this.keycloakAuth.init({ onLoad: 'login-required' }).then((authenticated) => {
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

