import { Component, OnInit } from '@angular/core';
import { CustomersServiceController } from '../controller/customers.service';
import { CustomerApi } from '../../../api/generated';

@Component({
  selector: 'app-customers-page',
  templateUrl: './customers-page.component.html',
  styleUrl: './customers-page.component.css',
  standalone: false
})

export class CustomersPageComponent implements OnInit {
  customers: CustomerApi.Customer[] = [];
  loading = false;
  error?: string;
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  constructor(private customersService: CustomersServiceController) {}

  ngOnInit(): void {
    this.load();
  }

  load(page: number = this.page) {
    this.loading = true;
    this.error = undefined;
    this.customersService.getCustomers(page, this.size).subscribe({
      next: (resp: CustomerApi.PageCustomer) => {
        this.customers = resp.content ?? [];
        this.page = resp.number ?? 0;
        this.size = resp.size ?? 10;
        this.totalPages = resp.totalPages ?? 0;
        this.totalElements = resp.totalElements ?? 0;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.message || 'Nepodařilo se načíst zákazníky';
        this.loading = false;
      }
    });
  }

  prev(): void {
    if (this.page > 0) {
      this.load(this.page - 1);
    }
  }

  next(): void {
    if (this.page + 1 < this.totalPages) {
      this.load(this.page + 1);
    }
  }
}
