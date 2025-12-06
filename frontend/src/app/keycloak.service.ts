// frontend/src/app/keycloak.service.ts
import { Injectable } from '@angular/core';
import Keycloak, {KeycloakInstance, KeycloakInitOptions, KeycloakProfile} from 'keycloak-js';
import {HttpClient} from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private keycloakAuth!: KeycloakInstance;
  private refreshTimeoutId: number | null = null;
  private readonly refreshMinValidity = 30;
  public hasAdminAccess: boolean = false;
  public currentUser: KeycloakProfile | null = null;

  constructor(public readonly http: HttpClient) {
  }

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
          this.hasAdminAccess = this.hasAnyRole(['accountant','manager']);
          try {
            this.scheduleTokenRefresh();
          } catch (e) {
            console.warn('Nepodařilo se naplánovat automatické obnovení tokenu', e);
          }
          this.keycloakAuth.loadUserProfile().then((currentUser)=>{
            this.currentUser = currentUser;
            resolve();
          })
        } else {
          reject('Neautentizováno');
          resolve();
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
    this.clearRefreshTimer();
    this.keycloakAuth.logout();
  }

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

  public getUserRoles(): string[] {
    if (!this.keycloakAuth || !this.keycloakAuth.tokenParsed) {
      return [];
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

    return Array.from(rolesSet);
  }

  private scheduleTokenRefresh(): void {
    this.clearRefreshTimer();

    if (!this.keycloakAuth || !this.keycloakAuth.tokenParsed) {
      this.refreshTimeoutId = window.setTimeout(() => this.callUpdateToken(), 60 * 1000);
      return;
    }

    try {
      const tokenParsed: any = this.keycloakAuth.tokenParsed;
      const exp = typeof tokenParsed.exp === 'number' ? tokenParsed.exp * 1000 : null;
      const now = Date.now();

      if (!exp) {
        this.refreshTimeoutId = window.setTimeout(() => this.callUpdateToken(), 60 * 1000);
        return;
      }

      const refreshAt = exp - (this.refreshMinValidity * 1000);
      const msUntilRefresh = Math.max(refreshAt - now, 0);

      if (msUntilRefresh <= 2000) {
        this.refreshTimeoutId = window.setTimeout(() => this.callUpdateToken(), 500);
      } else {
        this.refreshTimeoutId = window.setTimeout(() => this.callUpdateToken(), msUntilRefresh);
      }
    } catch (e) {
      console.warn('Chyba při plánování obnovení tokenu', e);
      this.refreshTimeoutId = window.setTimeout(() => this.callUpdateToken(), 60 * 1000);
    }
  }

  private clearRefreshTimer(): void {
    if (this.refreshTimeoutId != null) {
      try {
        clearTimeout(this.refreshTimeoutId);
      } catch (e) {
        // ignore
      }
      this.refreshTimeoutId = null;
    }
  }

  private callUpdateToken(): void {
    if (!this.keycloakAuth) {
      return;
    }

    this.keycloakAuth.updateToken(this.refreshMinValidity)
      .then((refreshed) => {
        this.scheduleTokenRefresh();
      })
      .catch((err) => {
        console.warn('Nezdařené obnovení tokenu, přesměrování na přihlášení', err);
        try {
          this.login();
        } catch (e) {
          console.error('Chyba při volání login() po neúspěšném updateToken', e);
        }
      });
  }

  get currentCustomerId(): number | null {
    const tokenParsed: any = this.keycloakAuth.tokenParsed;
    if (!tokenParsed) return null;
    console.log(tokenParsed);
    return Number(tokenParsed["db_id"] ?? null);
  }


}
