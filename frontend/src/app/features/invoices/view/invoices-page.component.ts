import { Component, OnInit } from '@angular/core';
import { InvoicesServiceController } from '../controller/invoices.service';
import { Invoice, PagedResponseInvoice } from '../../../api/generated';

@Component({
  selector: 'app-invoices-page',
  templateUrl: './invoices-page.component.html',
  styleUrl: './invoices-page.component.css',
  standalone: false
})
export class InvoicesPageComponent implements OnInit {
  invoices: Invoice[] = [];
  loading = false;
  error?: string;
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  constructor(private invoicesService: InvoicesServiceController) {}

  ngOnInit(): void {
    this.load();
  }

  load(page: number = this.page): void {
    this.loading = true;
    this.error = undefined;
    this.invoicesService.getInvoices(page, this.size).subscribe({
      next: (resp: PagedResponseInvoice) => {
        this.invoices = resp.content;
        this.page = resp.page;
        this.size = resp.size;
        this.totalPages = resp.totalPages;
        this.totalElements = resp.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Nepodařilo se načíst faktury';
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
