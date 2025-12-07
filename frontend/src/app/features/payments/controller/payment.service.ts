import { environment } from '../../../../environments/environment';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  OpenAPI,
  PaymentsService,
  CreatePaymentRequest,
  CreatePaymentResponse,
  CapturePaymentRequest,
  CapturePaymentResponse,
  PaymentStatusResponse
} from '../../../api/generated/payment';
import { catchError } from 'rxjs/operators';
import { handleError } from '../../common/controller/common.service';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class PaymentServiceController {
  private readonly paymentService = inject(PaymentsService);
  private readonly http = inject(HttpClient);

  constructor() {
    OpenAPI.BASE = environment.apiBase;
  }

  getPdf(invoiceId: number): Observable<Record<string, any>> {
    return this.paymentService.renderInvoice({ id: invoiceId }).pipe(
      catchError(handleError)
    );
  }

  getInvoicePdfAsBlob(invoiceId: number): Observable<Blob> {
    const pdfUrl = `${OpenAPI.BASE}/api/payments/invoice/${invoiceId}/render`;
    return this.http.get(pdfUrl, {
      responseType: 'blob'
    });
  }

  createPayment(request: CreatePaymentRequest): Observable<CreatePaymentResponse> {
    return this.paymentService.createPayment({ requestBody: request }).pipe(
      catchError(handleError)
    );
  }

  capturePayment(request: CapturePaymentRequest): Observable<CapturePaymentResponse> {
    return this.paymentService.capturePayment({ requestBody: request }).pipe(
      catchError(handleError)
    );
  }

  getPaymentStatus(paymentId: number): Observable<PaymentStatusResponse> {
    return this.paymentService.getPaymentStatus({ paymentId }).pipe(
      catchError(handleError)
    );
  }

  cancelPayment(paymentId: number): Observable<Record<string, any>> {
    return this.paymentService.cancelPayment({ paymentId }).pipe(
      catchError(handleError)
    );
  }
}
