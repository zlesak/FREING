import { environment } from '../../environments/environment';
import {inject, Injectable} from '@angular/core';
import { from, Observable } from 'rxjs';
import {OpenAPI, PaymentsService} from '../api/generated/payment';
import {catchError} from 'rxjs/operators';
import {handleError} from './common.service';

@Injectable({ providedIn: 'root' })
export class PaymentServiceController {
  private readonly paymentService = inject(PaymentsService);
  constructor() {
    OpenAPI.BASE = environment.apiBase;
  }

  getPdf(invoiceId: number): Observable<Record<string, any>> {
    return from(this.paymentService.renderInvoice({ id: invoiceId })).pipe(
      catchError(handleError)
    );
  }
}
