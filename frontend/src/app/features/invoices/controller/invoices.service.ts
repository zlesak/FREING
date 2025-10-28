import { environment } from '../../../../environments/environment';
import { Injectable } from '@angular/core';
import { from, Observable, throwError, map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { InvoiceApi } from '../../../api/generated';

@Injectable({ providedIn: 'root' })
export class InvoicesServiceController {
  constructor() {
    InvoiceApi.OpenAPI.BASE = environment.apiBase;
  }

  getInvoices(page = 0, size = 10): Observable<InvoiceApi.InvoicesPagedResponse> {
    return from(InvoiceApi.InvoicesService.getAllInvoices({ page, size })).pipe(
      catchError(this.handleError)
    );
  }

  getInvoice(id: number): Observable<InvoiceApi.Invoice> {
    return from(InvoiceApi.InvoicesService.getInvoice({ id })).pipe(catchError(this.handleError));
  }

  createInvoice(request: InvoiceApi.InvoiceCreateRequest): Observable<InvoiceApi.Invoice> {
    return from(InvoiceApi.InvoicesService.createInvoice({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  updateInvoice(id: number, request: InvoiceApi.InvoiceUpdateRequest): Observable<InvoiceApi.Invoice> {
    return from(InvoiceApi.InvoicesService.updateInvoice({ id, requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  deleteInvoice(id: number): Observable<void> {
    return from(InvoiceApi.InvoicesService.deleteInvoice({ id })).pipe(
      map(() => void 0),
      catchError(this.handleError)
    );
  }

  generateAggregatedReport(request: InvoiceApi.InvoiceReportRequest): Observable<InvoiceApi.AggregatedReportResponse> {
    return from(InvoiceApi.ReportingService.makeAggregatedReport({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  exportReportCsv(request: InvoiceApi.InvoiceReportRequest): Observable<Blob> {
    return from(InvoiceApi.ReportingService.exportAggregatedReportCsv({ requestBody: request })).pipe(
      map(text => new Blob([text], { type: 'text/csv;charset=utf-8' })),
      catchError(this.handleError)
    );
  }

  private handleError = (err: any) => {
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
}
