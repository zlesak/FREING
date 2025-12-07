import { AfterViewInit, Component, inject, OnInit, output, signal, ViewChild } from '@angular/core';
import { InvoicesServiceController } from '../../controller/invoices.service';
import { InvoiceApi } from '../../../../api/generated';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { DatePipe, CommonModule } from '@angular/common';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Invoice, PagedModelInvoice } from '../../../../api/generated/invoice';
import { getStatusColor } from '../../../home/view/home-page.component';
import { CommonFilterComponent } from '../../../common/filter/filter.component';
import { KeycloakService } from '../../../../security/keycloak.service';
import { PageTitleService } from '../../../common/controller/page-title.service';
import { InvoiceStatusTranslationService } from '../../../common/controller/invoice-status-translation.service';
import { CustomersServiceController } from '../../../customers/controller/customers.service';
import { CustomerDto } from '../../../../api/generated/customer/models/CustomerDto';
import { SuppliersServiceController } from '../../../suppliers/controller/suppliers.service';
import { SupplierDto } from '../../../../api/generated/customer/models/SupplierDto';
import { ResponsiveService } from '../../../common/controller/common.service';

export enum InvoiceStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED'
}

@Component({
  selector: 'app-invoices-page',
  standalone: true,
  templateUrl: './invoices-table.component.html',
  styleUrl: './invoices-table.component.css',
  imports: [CommonModule, MatButtonModule, MatDividerModule, MatIconModule, MatProgressBar, MatTableModule, DatePipe, MatSortModule, MatPaginatorModule, CommonFilterComponent],
})
export class InvoicesTableComponent implements OnInit, AfterViewInit {
    statusOptions = Object.values(InvoiceStatus);
    currencyOptions = ['CZK', 'EUR', 'USD'];
    customerList: { email: string, id: number }[] = [];
    filterValues: any = {};

    private readonly invoicesService = inject( InvoicesServiceController);
    private readonly pageTitleService = inject(PageTitleService);
    protected readonly keycloakService = inject( KeycloakService );
    protected readonly statusTranslation = inject(InvoiceStatusTranslationService);
    protected readonly router = inject(Router);
    outputData = output<Invoice[]>();
    protected dataSource = new MatTableDataSource<InvoiceApi.Invoice>([]);
    protected loading = signal<boolean>(false);
    protected error?: string;
    protected loadingCustomers = signal<boolean>(true);
    protected loadingSuppliers = signal<boolean>(true);
    protected readonly responsiveService = inject(ResponsiveService);

    protected totalElements = 0;
    protected currentPage = 0;
    protected currentSize = 10;
    @ViewChild(MatSort) sort!: MatSort;
    @ViewChild('paginator') paginator! : MatPaginator;

    private readonly customersService = inject(CustomersServiceController);
    private readonly suppliersService = inject(SuppliersServiceController);
    protected customersMap: Record<number, CustomerDto> = {};
    protected suppliersMap: Record<number, SupplierDto> = {};

    public displayedColumns: string[] = [
      'invoiceNumber',
      'referenceNumber',
      'customerName',
      'supplierId',
      'issueDate',
      'receiveDate',
      'dueDate',
      'amount',
      'status'
    ];

    ngOnInit(): void {
      this.pageTitleService.setTitle('Faktury');
      this.loadInvoicesPage(0, this.currentSize);
      this.loadAllCustomers();
    }

    // ngAfterViewInit already implemented above, remove duplicate

    loadAllCustomers() {
      this.customersService.getCustomers({ page: 0, size: 999 }).subscribe({
        next: (resp) => {
          if (resp.content) {
            this.customerList = resp.content.map((c: any) => ({ email: c.email, id: c.id }));
          }
        },
        error: () => {}
      });
    }

    onFilter(filter: any) {
      this.filterValues = filter;
      this.loadInvoicesPage(this.currentPage, this.currentSize);
    }

  ngAfterViewInit() {
    if (this.sort) {
      this.dataSource.sort = this.sort;
    }
    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
    }
  }
  loadInvoicesPage(page: number, size: number): void {
    this.loading.set(true);
    this.error = undefined;
    const filter = this.filterValues || {};
    this.invoicesService.getInvoices({
      page,
      size,
      dateFrom: filter.from || undefined,
      dateTo: filter.to || undefined,
      customerId: filter.customerId || undefined,
      status: (filter.status && [
        'DRAFT', 'PENDING', 'PAID', 'OVERDUE', 'CANCELLED', 'SENT', 'PAID_OVERDUE'
      ].includes(filter.status)) ? filter.status : undefined,
      amountFrom: filter.amountFrom || undefined,
      amountTo: filter.amountTo || undefined,
      currency: filter.currency || undefined
    }).subscribe({
      next: (resp: PagedModelInvoice) => {
        if(resp.content){
          this.dataSource.data = resp.content;
          this.outputData.emit(this.dataSource.data);
          this.totalElements = resp.page?.totalElements ?? resp.content.length;
          this.loadCustomersForInvoices();
          this.loadSuppliersForInvoices();
        }
        if (this.paginator) {
          this.paginator.length = this.totalElements;
          this.paginator.pageSize = size;
        }
        if (this.sort) this.dataSource.sort = this.sort;
        if (this.paginator) this.dataSource.paginator = this.paginator;
        this.loading.set(false);
      },
      error: (err) => {
        this.error = err.message || 'Nepodařilo se načíst faktury';
        this.loading.set(false);
      }
    });
  }
  loadSuppliersForInvoices(): void {
    this.loadingSuppliers.set(true);
    const invoiceSupplierIds = Array.from(new Set(this.dataSource.data.map(inv => inv.supplierId).filter(id => !!id)));
    if (!invoiceSupplierIds.length) {
      this.loadingSuppliers.set(false);
      return;
    }
    this.suppliersService.getSuppliersByIds(invoiceSupplierIds).subscribe({
      next: (resp) => {
        if (resp.content) {
          for (const s of resp.content) {
            this.suppliersMap[s.id] = s;
          }
        }
        this.loadingSuppliers.set(false);
      },
      error: (err) => {
        this.loadingSuppliers.set(false);
        console.log('Nepodařilo se načíst dodavatele:', err);
      }
    });
  }

  loadCustomersForInvoices(): void {
    this.loadingCustomers.set(true);
    const invoiceCustomerIds = Array.from(new Set(this.dataSource.data.map(inv => inv.customerId).filter(id => !!id)));
    if (!invoiceCustomerIds.length) {
      this.loadingCustomers.set(false);
      return;
    }
    this.customersService.getCustomers({ page: 0, size: invoiceCustomerIds.length, customerIds: invoiceCustomerIds }).subscribe({
      next: (resp) => {
        if (resp.content) {
          for (const c of resp.content) {
            this.customersMap[c.id] = c;
          }
        }
        this.loadingCustomers.set(false);
      },
      error: (err) => {
        this.loadingCustomers.set(false);
        console.log('Nepodařilo se načíst zákazníky:', err);
      }
    });
  }

  getCustomerDisplayName(id: number): string {
    const c = this.customersMap[id];
    if (!c) return id?.toString() ?? '';
    if (c.tradeName && c.tradeName.trim().length > 0) return c.tradeName;
    return (c.name + ' ' + c.surname).trim();
  }

  getSupplierDisplayName(id: number): string {
    const s = this.suppliersMap[id];
    if (!s) return id?.toString() ?? '';
    if (s.tradeName && s.tradeName.trim().length > 0) return s.tradeName;
    return s.email || s.phoneNumber || id?.toString() || '';
  }

  pageUpdate(event: PageEvent){
    this.currentPage = event.pageIndex;
    this.currentSize = event.pageSize;
    this.loadInvoicesPage(this.currentPage, this.currentSize);
  }

  async onInvoiceClick(invoice: any){
    console.log(invoice);
    await this.router.navigate(['/invoice', invoice.id]);
  }

  protected readonly getStatusColor = getStatusColor;
}
