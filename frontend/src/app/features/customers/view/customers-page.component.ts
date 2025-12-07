import { AfterViewChecked, Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { CustomersServiceController } from '../controller/customers.service';
import { CustomerApi } from '../../../api/generated';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import {Customer, CustomerDto} from '../../../api/generated/customer';
import { PageTitleService } from '../../../services/page-title.service';

@Component({
  selector: 'app-customers-page',
  standalone: true,
  templateUrl: './customers-page.component.html',
  styleUrl: './customers-page.component.css',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatProgressBar,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
  ],
})
export class CustomersPageComponent implements OnInit, AfterViewChecked {
  private readonly customersService = inject(CustomersServiceController);
  protected readonly router = inject(Router);
  private readonly pageTitleService = inject(PageTitleService);

  protected dataSource = new MatTableDataSource<CustomerDto>([]);
  protected loading = signal<boolean>(false);
  protected error?: string;
  customers: CustomerDto[] = [];
  page = 0;
  size = 10;
  totalPages = 0;

  protected totalElements = 0;
  protected currentPage = 0;
  protected currentSize = 10;

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns: string[] = [
    'index',
    'name',
    'surname',
    'email',
    'phoneNumber',
    'city'
  ];

  ngOnInit(): void {
    this.pageTitleService.setTitle('Zákazníci');
    this.loadAllCustomers();
  }

  ngAfterViewChecked(): void {
    if (this.sort && this.dataSource.sort !== this.sort) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      console.log('Sort initialized:', this.sort);
    }
  }

  loadAllCustomers(): void {
    this.loading.set(true);
    this.error = undefined;
    this.customersService.getCustomers(this.page, this.size).subscribe({
      next: (resp: CustomerApi.PagedModelCustomerDto) => {
        this.dataSource.data = resp.content ?? [];
        this.page = resp.page?.number ?? 0;
        this.size = resp.page?.size ?? 10;
        this.totalPages = resp.page?.totalPages ?? 0;
        this.totalElements = resp.page?.totalElements ?? 0;
        this.loading.set(false);
      },
      error: (err) => {
        this.error = err?.message || 'Nepodařilo se načíst zákazníky';
        this.loading.set(false);
      },
    });
  }

  pageUpdate(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.currentSize = event.pageSize;
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadAllCustomers();
  }
}
