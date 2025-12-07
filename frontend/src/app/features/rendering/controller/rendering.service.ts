import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { RenderingService } from '../../../api/generated/rendering/services/RenderingService';
import { PagePdfMetadataDto } from '../../../api/generated/rendering/models/PagePdfMetadataDto';
import { OpenAPI } from '../../../api/generated/rendering/core/OpenAPI';
import { environment } from '../../../../environments/environment';
import { InvoiceApi } from '../../../api/generated';
import { ReportingService } from '../../../api/generated/invoice/services/ReportingService';

@Injectable({
  providedIn: 'root',
})
export class RenderingServiceController {
  private readonly renderingService = inject(RenderingService);
  private readonly reportingService = inject(ReportingService);
  private readonly http = inject(HttpClient);

  constructor() {
    OpenAPI.BASE = environment.apiBase;
    InvoiceApi.OpenAPI.BASE = environment.apiBase;
  }

  getPdfs(params: {
    page?: number,
    size?: number,
    type?: string,
    filename?: string,
    generatedFrom?: string,
    generatedTo?: string,
    sort?: Array<string>,
  }): Observable<PagePdfMetadataDto> {
    return this.renderingService.listPdfs(params);
  }

  getPdfById(id: number): Observable<Blob> {
    return this.http.get(`${environment.apiBase}/api/rendering/${id}`, {
      responseType: 'blob'
    });
  }

  deletePdf(id: number): Observable<any> {
    return this.renderingService.deletePdf({ id });
  }

  generateInvoiceReport(request: InvoiceApi.InvoiceReportRequest): Observable<Blob> {
    return this.http.post(`${environment.apiBase}/api/invoices/report/pdf`, request, {
      responseType: 'blob'
    });
  }

  getLatestPdfId(): Observable<number> {
    return new Observable<number>(observer => {
      this.getPdfs({ page: 0, size: 1, sort: ['generatedAt,desc'] }).subscribe({
        next: (page) => {
          if (page.content && page.content.length > 0) {
            const latestPdf = page.content[0];
            observer.next(latestPdf.id!);
            observer.complete();
          } else {
            observer.error(new Error('Nepodařilo se najít nejnovější PDF'));
          }
        },
        error: (err) => observer.error(err)
      });
    });
  }
}

