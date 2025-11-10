import {AfterViewChecked, Component, inject, OnInit, signal, ViewChild} from '@angular/core';
import { InvoicesServiceController } from '../controller/invoices.service';
import { InvoiceApi } from '../../../api/generated';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';
import {MatProgressBar} from '@angular/material/progress-bar';
import {
  MatTableDataSource, MatTableModule
} from '@angular/material/table';
import {DatePipe, NgClass} from '@angular/common';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatPaginator, MatPaginatorModule, PageEvent} from '@angular/material/paginator';

@Component({
  selector: 'app-invoices-page',
  standalone: true,
  templateUrl: './invoices-page.component.html',
  styleUrl: './invoices-page.component.css',
  imports: [MatButtonModule, MatDividerModule, MatIconModule, MatProgressBar, MatTableModule, DatePipe, NgClass, MatSortModule, MatPaginatorModule],
})
export class InvoicesPageComponent implements OnInit, AfterViewChecked {
  private readonly invoicesService = inject( InvoicesServiceController);
  protected readonly router = inject(Router);

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
    this.loadAllInvoices();
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
}
