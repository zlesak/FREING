import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { KeycloakService } from './keycloak.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private keycloakService: KeycloakService) {}

  canActivate(): boolean {
    if (this.keycloakService.isLoggedIn()) {
      return true;
    } else {
      this.keycloakService.login();
      return false;
    }
  }
}
