import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable, map } from 'rxjs';
import { InvoiceApi } from '../../../api/generated';
@Injectable({ providedIn: 'root' })
export class ExchangeRatesController {
  private baseUrl = environment.apiBase + '/api/exchange';
  constructor(private http: HttpClient) {}

  convert(from: string, to: string, amount: number): Observable<InvoiceApi.CurrencyConversionResponse> {
    const params = new HttpParams()
      .set('from', from)
      .set('to', to)
      .set('amount', amount.toString());
    return this.http.get<InvoiceApi.CurrencyConversionResponse>(`${this.baseUrl}/convert`, { params });
  }

  getRate(from: string, to: string): Observable<number> {
    if (from.toUpperCase() === to.toUpperCase()) return new Observable(obs => { obs.next(1); obs.complete(); });
    return this.convert(from, to, 1).pipe(map(r => r.rate));
  }
}
