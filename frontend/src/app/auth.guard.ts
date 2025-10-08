import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { KeycloakService } from './keycloak.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private keycloakService: KeycloakService) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!this.keycloakService.isLoggedIn()) {
      this.keycloakService.login();
      return false;
    }

    const requiredRoles: string[] | undefined = route.data && route.data['roles'];
    if (requiredRoles && requiredRoles.length > 0) {
      const allowed = this.keycloakService.hasAnyRole(requiredRoles);
      if (!allowed) {
        // uživatel je přihlášen, ale nemá potřebnou roli
        console.warn('Unauthorized - missing required role(s):', requiredRoles);
        return false;
      }
    }

    return true;
  }
}
