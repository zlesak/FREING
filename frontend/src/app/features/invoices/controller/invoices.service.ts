import { environment } from '../../../../environments/environment';
import {inject, Injectable} from '@angular/core';
import { from, Observable, throwError, map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { InvoiceApi } from '../../../api/generated';
import {InvoicesService, ReportingService} from '../../../api/generated/invoice';

@Injectable({ providedIn: 'root' })
export class InvoicesServiceController {
  private readonly invoicesService = inject(InvoicesService);
  private readonly reportingService = inject(ReportingService);
  constructor() {
    InvoiceApi.OpenAPI.BASE = environment.apiBase;
  }

  getInvoices(page = 0, size = 10): Observable<InvoiceApi.PagedModelInvoice> {
    return from(this.invoicesService.getAllInvoices({ page, size })).pipe(
      catchError(this.handleError)
    );
  }

  getInvoice(id: number): Observable<InvoiceApi.Invoice> {
    return from(this.invoicesService.getInvoice({ id })).pipe(catchError(this.handleError));
  }

  createInvoice(request: InvoiceApi.InvoiceCreateRequest): Observable<InvoiceApi.Invoice> {
    return from(this.invoicesService.createInvoice({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  updateInvoice(id: number, request: InvoiceApi.InvoiceUpdateRequest): Observable<InvoiceApi.Invoice> {
    return from(this.invoicesService.updateInvoice({ id, requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  deleteInvoice(id: number): Observable<void> {
    return from(this.invoicesService.deleteInvoice({ id })).pipe(
      map(() => void 0),
      catchError(this.handleError)
    );
  }

  generateAggregatedReport(request: InvoiceApi.InvoiceReportRequest): Observable<InvoiceApi.AggregatedReportResponse> {
    return from(this.reportingService.makeAggregatedReport({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  exportReportCsv(request: InvoiceApi.InvoiceReportRequest): Observable<Blob> {
    return from(this.reportingService.exportAggregatedReportCsv({ requestBody: request })).pipe(
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
