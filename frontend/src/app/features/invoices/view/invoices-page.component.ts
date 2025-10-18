import {Component, inject, OnInit, signal} from '@angular/core';
import { InvoicesServiceController } from '../controller/invoices.service';
import { InvoiceApi } from '../../../api/generated';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';

@Component({
  selector: 'app-invoices-page',
  standalone: true,
  templateUrl: './invoices-page.component.html',
  styleUrl: './invoices-page.component.css',
  imports: [MatButtonModule, MatDividerModule, MatIconModule],
})
export class InvoicesPageComponent implements OnInit {
  private readonly invoicesService = inject( InvoicesServiceController);
  protected readonly router = inject(Router);

  protected invoices: InvoiceApi.Invoice[] = [];
  protected loading = signal<boolean>(false);
  protected error?: string;
  protected page = 0;
  protected size = 10;
  protected totalPages = 0;
  protected totalElements = 0;

  ngOnInit(): void {
    this.load();
  }

load(page: number = this.page): void {
  this.loading.set(true);
  this.error = undefined;
  this.invoicesService.getInvoices(page, this.size).subscribe({
    next: (resp: InvoiceApi.PagedModelInvoice) => {
      this.invoices = resp.content ?? [];
      this.page = resp.page?.number ?? 0;
      this.size = resp.page?.size ?? 10;
      this.totalPages = resp.page?.totalPages ?? 0;
      this.totalElements = resp.page?.totalPages ?? 0;
      this.loading.set(false);
    },
    error: (err) => {
      this.error = err.message || 'Nepodařilo se načíst faktury';
      this.loading.set(false);
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
