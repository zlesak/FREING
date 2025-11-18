import { Injectable } from '@angular/core';
import { Observable, from } from 'rxjs';
import { map } from 'rxjs/operators';
import { InvoiceApi } from '../../../api/generated';

@Injectable({ providedIn: 'root' })
export class ExchangeRatesController {
  constructor() {}

  convert(fromCurrency: string, toCurrency: string, amount: number): Observable<InvoiceApi.CurrencyConversionResponse> {
    return from(InvoiceApi.ExchangeService.convert({ from: fromCurrency, to: toCurrency, amount }));
  }

  getRate(fromCurrency: string, toCurrency: string): Observable<number> {
    return from(InvoiceApi.ExchangeService.getRate({ from: fromCurrency, to: toCurrency })).pipe(
      map(response => response.rate)
    );
  }
}
