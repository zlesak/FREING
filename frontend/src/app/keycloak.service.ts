// frontend/src/app/keycloak.service.ts
import { Injectable } from '@angular/core';
import Keycloak, { KeycloakInstance, KeycloakInitOptions } from 'keycloak-js';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private keycloakAuth!: KeycloakInstance;

  public async init(): Promise<void> {
    this.keycloakAuth = new Keycloak({
      url: 'http://auth.freing.test/',
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

  // Přidáno: zkontroluje, zda token obsahuje některou z požadovaných rolí
  public hasAnyRole(requiredRoles: string[]): boolean {
    if (!this.keycloakAuth || !this.keycloakAuth.tokenParsed) {
      return false;
    }

    const tokenParsed: any = this.keycloakAuth.tokenParsed;
    const rolesSet = new Set<string>();

    if (tokenParsed.realm_access && Array.isArray(tokenParsed.realm_access.roles)) {
      tokenParsed.realm_access.roles.forEach((r: string) => rolesSet.add(r));
    }

    if (tokenParsed.resource_access && typeof tokenParsed.resource_access === 'object') {
      Object.values(tokenParsed.resource_access).forEach((client: any) => {
        if (client && Array.isArray(client.roles)) {
          client.roles.forEach((r: string) => rolesSet.add(r));
        }
      });
    }

    return requiredRoles.some(r => rolesSet.has(r));
  }
}
