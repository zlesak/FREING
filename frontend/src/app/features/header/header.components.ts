import {Component, inject} from '@angular/core';
import { KeycloakService } from '../../keycloak.service';
import {RouterLink, RouterLinkActive} from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.components.html',
  styleUrl: './header.components.css',
  imports: [
    RouterLinkActive,
    RouterLink
  ],
  standalone: true
})
export class HeaderComponent {
  protected readonly keycloakService = inject(KeycloakService);
  protected readonly title: string = 'FREING';

  logout(): void {
    this.keycloakService.logout();
  }

  getUserRole(): string {
    const roles = this.keycloakService.getUserRoles();

    if (roles.includes('manager')) {
      return 'Manažer';
    }
    if (roles.includes('accountant')) {
      return 'Účetní';
    }
    if (roles.includes('customer')) {
      return 'Zákazník';
    }

    return 'Uživatel';
  }

  getUserName(): string {
    const user = this.keycloakService.currentUser;
    if (!user) return 'Nepřihlášen';

    if (user.firstName && user.lastName) {
      return `${user.firstName} ${user.lastName}`;
    }
    if (user.username) {
      return user.username;
    }
    if (user.email) {
      return user.email;
    }
    return 'Uživatel';
  }
}
