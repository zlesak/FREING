import { Injectable, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';

/**
 * Service pro správu titulků stránek
 */
@Injectable({
  providedIn: 'root'
})
export class PageTitleService {
  private readonly titleService = inject(Title);
  private readonly appName = 'FREING';

  /**
   * Nastaví titulek stránky s automatickým přidáním názvu aplikace
   * @param title Titulek stránky
   */
  setTitle(title: string): void {
    this.titleService.setTitle(`${title} | ${this.appName}`);
  }

  /**
   * Nastaví pouze název aplikace jako titulek
   */
  setDefaultTitle(): void {
    this.titleService.setTitle(this.appName);
  }

  /**
   * Získá aktuální titulek
   */
  getTitle(): string {
    return this.titleService.getTitle();
  }
}

