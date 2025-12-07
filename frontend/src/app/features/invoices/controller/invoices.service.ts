
import { environment } from '../../../../environments/environment';
import { inject, Injectable } from '@angular/core';
import { from, Observable, map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { InvoiceApi } from '../../../api/generated';
import { InvoicesService, ReportingService } from '../../../api/generated/invoice';
import { handleError } from '../../common/controller/common.service';

@Injectable({ providedIn: 'root' })
export class InvoicesServiceController {
  private readonly invoicesService = inject(InvoicesService);
  private readonly reportingService = inject(ReportingService);
  constructor() {
    InvoiceApi.OpenAPI.BASE = environment.apiBase;
  }

  getInvoices(params: {
    page?: number,
    size?: number,
    dateFrom?: string,
    dateTo?: string,
    customerId?: number,
    status?: 'DRAFT' | 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED' | 'SENT' | 'PAID_OVERDUE',
    amountFrom?: number,
    amountTo?: number,
    currency?: string
  } = {}): Observable<InvoiceApi.PagedModelInvoice> {
    return from(this.invoicesService.getInvoices(params)).pipe(
      catchError(handleError)
    );
  }

  getInvoice(id: number): Observable<InvoiceApi.Invoice> {
    return from(this.invoicesService.getInvoice({ id })).pipe(catchError(handleError));
  }

  createInvoice(request: InvoiceApi.InvoiceCreateRequest): Observable<InvoiceApi.Invoice> {
    return from(this.invoicesService.createInvoice({ requestBody: request })).pipe(
      catchError(handleError)
    );
  }

  updateInvoice(id: number, request: InvoiceApi.InvoiceUpdateRequest): Observable<InvoiceApi.Invoice> {
    return from(this.invoicesService.updateInvoice({ id, requestBody: request })).pipe(
      catchError(handleError)
    );
  }

  deleteInvoice(id: number): Observable<void> {
    return from(this.invoicesService.deleteInvoice({ id })).pipe(
      map(() => void 0),
      catchError(handleError)
    );
  }
  markRead(id: number): Observable<void> {
    return from(this.invoicesService.markInvoiceAsRead({ id })).pipe(
      map(() => void 0),
      catchError(handleError)
    );
  }

  generateAggregatedReport(request: InvoiceApi.InvoiceReportRequest): Observable<InvoiceApi.AggregatedReportResponse> {
    return from(this.reportingService.makeAggregatedReport({ requestBody: request })).pipe(
      catchError(handleError)
    );
  }

  exportReportCsv(request: InvoiceApi.InvoiceReportRequest): Observable<Blob> {
    return from(this.reportingService.exportAggregatedReportCsv({ requestBody: request })).pipe(
      map(text => new Blob([text], { type: 'text/csv;charset=utf-8' })),
      catchError(handleError)
    );
  }

}
