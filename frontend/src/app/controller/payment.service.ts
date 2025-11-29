import { environment } from '../../environments/environment';
import {inject, Injectable} from '@angular/core';
import { from, Observable } from 'rxjs';
import {OpenAPI, PaymentsService} from '../api/generated/payment';
import {catchError} from 'rxjs/operators';
import {handleError} from './common.service';
import {HttpClient} from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class PaymentServiceController {
  private readonly paymentService = inject(PaymentsService);
  private readonly http = inject(HttpClient);
  constructor() {
    OpenAPI.BASE = environment.apiBase;
  }

  getPdf(invoiceId: number): Observable<Record<string, any>> {
    return from(this.paymentService.renderInvoice({ id: invoiceId })).pipe(
      catchError(handleError)
    );
  }

  getInvoicePdfAsBlob(invoiceId: number): Observable<Blob> {
    const pdfUrl = `${OpenAPI.BASE}/api/payments/invoice/${invoiceId}/render`;

    return this.http.get(pdfUrl, {
      responseType: 'blob'
    });
  }
}
