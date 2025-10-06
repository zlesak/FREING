import { Component } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.components.html',
  styleUrl: './header.components.css',
  standalone: false
})
export class HeaderComponent {
  title: string = 'FREING';
}
