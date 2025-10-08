import { Component } from '@angular/core';
import { KeycloakService } from '../../keycloak.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.components.html',
  styleUrl: './header.components.css',
  standalone: false
})
export class HeaderComponent {
  title: string = 'FREING';

  constructor(private keycloakService: KeycloakService) {}

  logout(): void {
    this.keycloakService.logout();
  }
}
