import { Injectable, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class PageTitleService {
  private readonly titleService = inject(Title);
  private readonly appName = 'FREING';

  setTitle(title: string): void {
    this.titleService.setTitle(`${title} | ${this.appName}`);
  }

  setDefaultTitle(): void {
    this.titleService.setTitle(this.appName);
  }

  getTitle(): string {
    return this.titleService.getTitle();
  }
}

