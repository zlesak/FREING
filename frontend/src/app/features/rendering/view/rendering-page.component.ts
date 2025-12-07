import { AfterViewChecked, Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { RenderingServiceController } from '../controller/rendering.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { PagePdfMetadataDto } from '../../../api/generated/rendering/models/PagePdfMetadataDto';
import { PageTitleService } from '../../common/controller/page-title.service';
import { ResponsiveService } from '../../common/controller/common.service';
import { InvoiceReportFilterComponent } from '../components/invoice-report-filter.component';
import { InvoiceApi } from '../../../api/generated';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomersServiceController } from '../../customers/controller/customers.service';

@Component({
  selector: 'app-rendering-page',
  standalone: true,
  templateUrl: './rendering-page.component.html',
  styleUrls: ['../../common/common-table-cards.css'],
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBar,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    InvoiceReportFilterComponent,
  ],
})
export class RenderingPageComponent implements OnInit, AfterViewChecked {
  filterValues: any = {};
  private readonly renderingService = inject(RenderingServiceController);
  protected readonly router = inject(Router);
  private readonly pageTitleService = inject(PageTitleService);
  protected readonly responsiveService = inject(ResponsiveService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly customersService = inject(CustomersServiceController);

  protected dataSource = new MatTableDataSource<any>([]);
  protected loading = signal<boolean>(false);
  protected error?: string;
  protected generating = signal<boolean>(false);
  protected customerList: { email: string, id: number }[] = [];

  protected totalElements = 0;
  protected currentPage = 0;
  protected currentSize = 10;

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(InvoiceReportFilterComponent) reportFilter?: InvoiceReportFilterComponent;

  displayedColumns: string[] = [
    'filename',
    'generatedAt',
    'actions'
  ];

  ngOnInit(): void {
    this.pageTitleService.setTitle('Vygenerované PDF');
    this.loadAllPdfs();
    this.loadAllCustomers();
  }

  ngAfterViewChecked(): void {
    if (this.sort && this.dataSource.sort !== this.sort) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    }
  }

  loadAllPdfs(): void {
    this.loading.set(true);
    this.error = undefined;
    const params = {
      page: this.currentPage,
      size: this.currentSize,
      ...this.filterValues
    };
    this.renderingService.getPdfs(params).subscribe({
      next: (resp: PagePdfMetadataDto) => {
        this.dataSource.data = resp.content ?? [];
        this.currentPage = resp.number ?? 0;
        this.currentSize = resp.size ?? 10;
        this.totalElements = resp.totalElements ?? 0;
        this.loading.set(false);
      },
      error: (err) => {
        this.error = err?.message || 'Nepodařilo se načíst PDF';
        this.loading.set(false);
      },
    });
  }

  onFilter(values: any) {
    this.filterValues = values;
    this.loadAllPdfs();
  }

  pageUpdate(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.currentSize = event.pageSize;
    this.loadAllPdfs();
  }

  openPdf(id: number): void {
    this.router.navigate(['/rendering/view', id]);
  }

  deletePdf(id: number): void {
    this.loading.set(true);
    this.renderingService.deletePdf(id).subscribe({
      next: () => this.loadAllPdfs(),
      error: (err) => {
        this.error = err?.message || 'Nepodařilo se smazat PDF';
        this.loading.set(false);
      },
    });
  }

  loadAllCustomers(): void {
    this.customersService.getCustomers({ page: 0, size: 999 }).subscribe({
      next: (resp) => {
        if (resp.content) {
          this.customerList = resp.content.map((c: any) => ({ email: c.email, id: c.id }));
        }
      },
      error: () => {}
    });
  }

  onGenerateReport(request: InvoiceApi.InvoiceReportRequest): void {
    this.generating.set(true);
    this.reportFilter?.setGenerating(true);

    this.renderingService.generateInvoiceReport(request).subscribe({
      next: (pdfBlob) => {
        console.log('PDF vygenerováno, velikost:', pdfBlob.size, 'bytes');

        const blobUrl = URL.createObjectURL(pdfBlob);
        window.open(blobUrl, '_blank');

        this.snackBar.open('Report byl úspěšně vygenerován a otevřen v novém okně', 'OK', { duration: 3000 });
        this.generating.set(false);
        this.reportFilter?.setGenerating(false);

        setTimeout(() => {
          this.loadAllPdfs();
        }, 2000);
      },
      error: (err) => {
        console.error('Chyba při generování reportu:', err);
        this.snackBar.open('Chyba při generování reportu: ' + (err.message || 'Neznámá chyba'), 'ERROR', {
          duration: 5000
        });
        this.generating.set(false);
        this.reportFilter?.setGenerating(false);
      },
    });
  }
}
