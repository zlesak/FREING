import { environment } from '../../../../environments/environment';
import { Injectable } from '@angular/core';
import { from, Observable, throwError, map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  InvoicesService as GenInvoicesService,
  ReportingService as GenReportingService,
  OpenAPI,
  Invoice,
  InvoiceCreateRequest,
  InvoiceUpdateRequest,
  InvoiceReportRequest,
  AggregatedReportResponse,
  InvoicesPagedResponse
} from '../../../api/generated';

@Injectable({ providedIn: 'root' })
export class InvoicesServiceController {
  constructor() {
    OpenAPI.BASE = environment.apiBase;
  }

  getInvoices(page = 0, size = 10): Observable<InvoicesPagedResponse> {
    return from(GenInvoicesService.getAllInvoices({ page, size })).pipe(
      catchError(this.handleError)
    );
  }

  getInvoice(id: number): Observable<Invoice> {
    return from(GenInvoicesService.getInvoice({ id })).pipe(catchError(this.handleError));
  }

  createInvoice(request: InvoiceCreateRequest): Observable<Invoice> {
    return from(GenInvoicesService.createInvoice({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  updateInvoice(id: number, request: InvoiceUpdateRequest): Observable<Invoice> {
    return from(GenInvoicesService.updateInvoice({ id, requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  deleteInvoice(id: number): Observable<void> {
    return from(GenInvoicesService.deleteInvoice({ id })).pipe(
      map(() => void 0),
      catchError(this.handleError)
    );
  }

  generateAggregatedReport(request: InvoiceReportRequest): Observable<AggregatedReportResponse> {
    return from(GenReportingService.makeAggregatedReport({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

// making blob from text returned from the API
  exportReportCsv(request: InvoiceReportRequest): Observable<Blob> {
    return from(GenReportingService.exportAggregatedReportCsv({ requestBody: request })).pipe(
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
