import { AfterViewChecked, Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { SuppliersServiceController } from '../controller/suppliers.service';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { SupplierDto, PagedModelSupplierDto } from '../../../api/generated/customer';
import { PageTitleService } from '../../../services/page-title.service';

@Component({
  selector: 'app-suppliers-page',
  standalone: true,
  templateUrl: './suppliers-page.component.html',
  styleUrl: './suppliers-page.component.css',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatProgressBar,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
  ],
})
export class SuppliersPageComponent implements OnInit, AfterViewChecked {
  private readonly suppliersService = inject(SuppliersServiceController);
  protected readonly router = inject(Router);
  private readonly pageTitleService = inject(PageTitleService);

  protected dataSource = new MatTableDataSource<SupplierDto>([]);
  protected loading = signal<boolean>(false);
  protected error?: string;

  protected totalElements = 0;
  protected currentPage = 0;
  protected currentSize = 10;

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns: string[] = [
    'index',
    'tradeName',
    'email',
    'phoneNumber',
    'city',
    'ico',
    'dic'
  ];

  ngOnInit(): void {
    this.pageTitleService.setTitle('Dodavatelé');
    this.loadAllSuppliers();
  }

  ngAfterViewChecked(): void {
    if (this.sort && this.dataSource.sort !== this.sort) {
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    }
  }

  loadAllSuppliers(): void {
    this.loading.set(true);
    this.error = undefined;
    this.suppliersService.getSuppliers(this.currentPage, this.currentSize).subscribe({
      next: (resp: PagedModelSupplierDto) => {
        this.dataSource.data = resp.content ?? [];
        this.currentPage = resp.page?.number ?? 0;
        this.currentSize = resp.page?.size ?? 10;
        this.totalElements = resp.page?.totalElements ?? 0;
        this.loading.set(false);
      },
      error: (err) => {
        this.error = err?.message || 'Nepodařilo se načíst dodavatele';
        this.loading.set(false);
      },
    });
  }

  pageUpdate(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.currentSize = event.pageSize;
    this.loadAllSuppliers();
  }
}

