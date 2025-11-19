import {
  AfterViewChecked, Component,
  inject, OnInit, output, signal, ViewChild
} from '@angular/core';
import { InvoicesServiceController } from '../../../../controller/invoices.service';
import { InvoiceApi } from '../../../../api/generated';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';
import {MatProgressBar} from '@angular/material/progress-bar';
import {
  MatTableDataSource, MatTableModule
} from '@angular/material/table';
import {DatePipe} from '@angular/common';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatPaginator, MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {Invoice} from '../../../../api/generated/invoice';
import {getStatusColor} from '../../../home/view/home-page.component';
import {KeycloakService} from '../../../../keycloak.service';

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
  imports: [MatButtonModule, MatDividerModule, MatIconModule, MatProgressBar, MatTableModule, DatePipe, MatSortModule, MatPaginatorModule],
})
export class InvoicesTableComponent implements OnInit, AfterViewChecked {
  private readonly invoicesService = inject( InvoicesServiceController);
  protected readonly keycloakService = inject( KeycloakService );
  protected readonly router = inject(Router);
  outputData = output<Invoice[]>()
  protected dataSource = new MatTableDataSource<InvoiceApi.Invoice>([]);
  protected loading = signal<boolean>(false);
  protected error?: string;

  protected totalElements = 0;
  protected currentPage = 0;
  protected currentSize = 10;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild('paginator') paginator! : MatPaginator;

  public displayedColumns: string[] = [
    'invoiceNumber',
    'customerName',
    'issueDate',
    'dueDate',
    'amount',
    'status'
  ];

  ngOnInit(): void {
    if (this.keycloakService.hasAdminAccess){
      this.loadAllInvoices();
    } else {
      this.loadInvoicesForUser();
    }
  }

  ngAfterViewChecked() {
    if (this.sort && this.dataSource.sort !== this.sort) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    }
  }
  loadAllInvoices(): void {
    this.loading.set(true);
    this.error = undefined;
    this.invoicesService.getInvoices(0, 999).subscribe({
      next: (resp: InvoiceApi.InvoicesPagedResponse) => {

        this.dataSource.data = resp.content;

        this.totalElements = resp.totalElements;
        this.outputData.emit(this.dataSource.data);
        if (this.paginator) {
          this.paginator.length = this.totalElements;
          this.paginator.pageSize = this.currentSize;
          this.paginator.pageIndex = 0;
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

  loadInvoicesForUser(){
    this.loading.set(true);
    this.error = undefined;
    const userId = this.keycloakService.currentUser?.id;
    if (!userId){
      console.log('user ID missing!');
      this.error = 'Error - user data missing';
      this.loading.set(false);
      return;
    }
    this.invoicesService.getInvoices(0, 999).subscribe({
      next: (resp: InvoiceApi.InvoicesPagedResponse) => {
        //TODO: get all invoices for user, when API is ready
        this.dataSource.data = resp.content;

        this.totalElements = resp.totalElements;
        this.outputData.emit(this.dataSource.data);
        if (this.paginator) {
          this.paginator.length = this.totalElements;
          this.paginator.pageSize = this.currentSize;
          this.paginator.pageIndex = 0;
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

  pageUpdate(event: PageEvent){
    this.currentPage = event.pageIndex;
    this.currentSize = event.pageSize;
  }

  async onInvoiceClick(invoice: any){
    console.log(invoice);
    await this.router.navigate(['/invoice', invoice.id]);
  }

  protected readonly getStatusColor = getStatusColor;
}
