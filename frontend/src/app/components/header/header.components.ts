import { Component } from '@angular/core';
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
  title: string = 'FREING';

  constructor(private keycloakService: KeycloakService) {}

  logout(): void {
    this.keycloakService.logout();
  }
}
