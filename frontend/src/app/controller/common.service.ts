import {throwError} from 'rxjs';

import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ResponsiveService {
  private mobileBreakpoint = 800;
  private isMobileSubject = new BehaviorSubject<boolean>(window.innerWidth < this.mobileBreakpoint);

  public isMobile$ = this.isMobileSubject.asObservable();
  public isMobile = this.isMobileSubject.value;

  constructor() {
    // Update at startup
    this.checkWidth(window.innerWidth);

    // Listen to resize events
    window.addEventListener('resize', () => {
      this.checkWidth(window.innerWidth);
    });
  }

  private checkWidth(width: number) {
    const mobile = width < this.mobileBreakpoint;
    this.isMobileSubject.next(mobile);
    this.isMobile = mobile;
  }
}


export const  handleError = (err: any) => {
  let message = 'Neznámá chyba';
  if (err && typeof err === 'object') {
    if ('body' in err && err.body && typeof err.body === 'object') {
      message = (err.body as any).message || JSON.stringify(err.body);
    } else if ('message' in err) {
      message = (err as any).message;
    }
  }
  return throwError(() => new Error(message));
};
