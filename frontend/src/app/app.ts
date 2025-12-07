import { Component } from '@angular/core';
import {HeaderComponent} from './features/header/header.components';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: true,
  imports: [
    HeaderComponent,
    RouterOutlet
  ],
  styleUrl: './app.css'
})
export class App {}
