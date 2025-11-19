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
}
