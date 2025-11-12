import { Component, computed, inject, signal } from '@angular/core';
import { KeycloakService } from '../../../keycloak.service';
import { InvoicesTableComponent } from '../../invoices/components/invoices-table/invoices-table.component';
import { InvoiceChartPie } from '../../invoices/components/invoice-chart-pie/invoice-chart-pie';
import { Invoice } from '../../../api/generated/invoice';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [
    CommonModule,
    InvoicesTableComponent,
    InvoiceChartPie
  ],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {
  protected readonly keycloakService = inject(KeycloakService);

  protected invoices = signal<Invoice[]>([]);

  protected chartDataStatuses = computed(() => {
    const statusCount: Record<string, { occurrence: number; color: string }> = {};

    this.invoices().forEach((invoice) => {
      const status = invoice.status ?? 'Unknown';
      if (!statusCount[status]) {
        statusCount[status] = { occurrence: 0, color: getStatusColor(status).background };
      }
      statusCount[status].occurrence += 1;
    });

    return Object.entries(statusCount).map(([status, { occurrence, color }]) => ({
      status,
      occurrence,
      color,
    }));
  });

  protected chartDataCurrency = computed(() => {
    const currencyCount: Record<string, { occurrence: number; color: string }> = {};

    this.invoices().forEach((invoice) => {
      const currency = invoice.currency ?? 'Unknown';
      if (!currencyCount[currency]) {
        currencyCount[currency] = { occurrence: 0, color: getCurrencyColor(currency) };
      }
      currencyCount[currency].occurrence += 1;
    });

    return Object.entries(currencyCount).map(([currency, { occurrence, color }]) => ({
      status: currency, // keep "status" field to fit chart input
      occurrence,
      color, // attach the color directly
    }));
  });

  getInvoices(invoices: Invoice[]): void {
    this.invoices.set(invoices);
  }
}

export function getStatusColor(status: string): { background: string; color: string } {
  switch (status.toUpperCase()) {
    case 'DRAFT':
      return { background: '#f1eac1', color: '#5a5c44' };
    case 'PENDING':
      return { background: '#e5e7eb', color: '#374151' };
    case 'PAID':
      return { background: '#d1fae5', color: '#065f46' };
    case 'OVERDUE':
      return { background: '#fee2e2', color: '#991b1b' };
    case 'CANCELLED':
      return { background: '#ac9090', color: '#834646' };
    default:
      return { background: '#f3f4f6', color: '#111827' };
  }
}
export function getCurrencyColor(currency: string): string {
  const colors: Record<string, string> = {
    USD: '#4caf50',
    EUR: '#2196f3',
    CZK: '#ffc107',
    GBP: '#9c27b0',
  };
  return colors[currency.toUpperCase()] ?? '#b0bec5'; // default gray
}
