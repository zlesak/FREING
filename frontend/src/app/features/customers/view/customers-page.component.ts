import {Component, inject, OnInit} from '@angular/core';
import { CustomersServiceController } from '../controller/customers.service';
import { CustomerApi } from '../../../api/generated';
import {Router} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-customers-page',
  templateUrl: './customers-page.component.html',
  styleUrl: './customers-page.component.css',
  imports: [
    MatButton,
    MatIcon
  ],
  standalone: true
})

export class CustomersPageComponent implements OnInit {
  protected readonly router = inject(Router);
  customers: CustomerApi.CustomerDto[] = [];
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
      next: (resp: CustomerApi.PagedModelCustomerDto) => {
        this.customers = resp.content ?? [];
        this.page = resp.page?.number ?? 0;
        this.size = resp.page?.size ?? 10;
        this.totalPages = resp.page?.totalPages ?? 0;
        this.totalElements = resp.page?.totalElements ?? 0;
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
