import {inject, Injectable} from '@angular/core';
import { Observable, from } from 'rxjs';
import { map } from 'rxjs/operators';
import { InvoiceApi } from '../../../api/generated';
import {ExchangeService} from '../../../api/generated/invoice';

@Injectable({ providedIn: 'root' })
export class ExchangeRatesController {
  private exchangeService = inject(ExchangeService);
  constructor() {}

  convert(fromCurrency: string, toCurrency: string, amount: number): Observable<InvoiceApi.CurrencyConversionResponse> {
    return from(this.exchangeService.convert({ from: fromCurrency, to: toCurrency, amount }));
  }

  getRate(fromCurrency: string, toCurrency: string): Observable<number> {
    return from(this.exchangeService.getRate({ from: fromCurrency, to: toCurrency })).pipe(
      map(response => response.rate)
    );
  }
}
