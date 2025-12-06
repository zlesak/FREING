import {Component, computed, inject, OnInit, signal} from '@angular/core';
import { KeycloakService } from '../../../keycloak.service';
import { InvoiceChartPie } from '../../invoices/components/invoice-chart-pie/invoice-chart-pie';
import {Invoice, PagedModelInvoice} from '../../../api/generated/invoice';
import { CommonModule } from '@angular/common';
import {InvoicesServiceController} from '../../../controller/invoices.service';
import {
  MatDatepickerModule,
} from '@angular/material/datepicker';
import {MatInputModule} from '@angular/material/input';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MatNativeDateModule, MatOptionModule} from '@angular/material/core';
import {firstValueFrom} from 'rxjs';
import {CustomersServiceController} from '../../customers/controller/customers.service';
import {CurrencyOptions, InvoiceStatus} from '../../common/Enums.js';
import {MatSelectModule} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatButton} from '@angular/material/button';
import {ResponsiveService} from '../../../controller/common.service';
import {InvoiceChartLine} from '../../invoices/components/invoice-chart-line/invoice-chart-line.component';
import { PageTitleService } from '../../../services/page-title.service';
import {InvoiceChartBar} from '../../invoices/components/invoice-chart-bar/invoice-chart-bar';
@Component({
  imports: [
    CommonModule,
    InvoiceChartPie,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatOptionModule,
    ReactiveFormsModule,
    MatButton,
    InvoiceChartLine,
    InvoiceChartBar,

  ],
  selector: 'app-home-page',
  standalone: true,
  styleUrl: './home-page.component.scss',
  templateUrl: './home-page.component.html'
})
export class HomePageComponent implements OnInit{
  private readonly invoicesService = inject(InvoicesServiceController);
  protected readonly keycloakService = inject(KeycloakService);
  private readonly customerService = inject(CustomersServiceController);
  protected readonly responsiveService = inject(ResponsiveService);
  private fb = inject(FormBuilder);
  protected invoices = signal<Invoice[]>([]);
  protected filteredInvoices = signal<Invoice[]>([]);
  protected loading = signal<boolean>(false);
  protected error?: string;
  private readonly pageTitleService = inject(PageTitleService);
  protected users: {email: string, id: number}[]= [];

  filterForm: FormGroup = this.fb.group({
    from: [null],
    to: [null],
    customerId: [null],
    status: [null],
    amountFrom: [null],
    amountTo: [null],
    currency: [null]
  });

  statusOptions = Object.values(InvoiceStatus);
  currencyOptions = Object.values(CurrencyOptions);

  protected chartDataStatuses = computed(() => {
    const statusCount: Record<string, { occurrence: number; color: string }> = {};

    this.filteredInvoices().forEach(invoice => {
      const status = invoice.status ?? 'Unknown';
      if (!statusCount[status]) {
        statusCount[status] = { occurrence: 0, color: getStatusColor(status).background };
      }
      statusCount[status].occurrence += 1;
    });

    return Object.entries(statusCount).map(([status, { occurrence, color }]) => ({
      itemName: status,
      occurrence,
      color,
    }));
  });

  protected chartDataCurrency = computed(() => {
    const currencyCount: Record<string, { occurrence: number; color: string }> = {};

    this.filteredInvoices().forEach(invoice => {
      const currency = invoice.currency ?? 'Unknown';
      if (!currencyCount[currency]) {
        currencyCount[currency] = { occurrence: 0, color: getCurrencyColor(currency) };
      }
      currencyCount[currency].occurrence += 1;
    });

    return Object.entries(currencyCount).map(([currency, { occurrence, color }]) => ({
      itemName: currency,
      occurrence,
      color,
    }));
  });

  protected chartDataCustomers = computed(() => {
    const customerCount: Record<string, number> = {};

    this.filteredInvoices().forEach(invoice => {
      const id = invoice.customerId ?? 'Unknown';
      customerCount[id] = (customerCount[id] || 0) + 1;
    });

    const sorted = Object.entries(customerCount).sort((a, b) => b[1] - a[1]);

    return sorted.map(([customerId, occurrence]) => ({
      itemName: this.resolveCustomerName(customerId),
      occurrence,
      color: hashIdToColor(customerId),
    }));
  });


  protected resolveCustomerName(customerId: string | number): string {
    const user = this.users.find(u => u.id === Number(customerId));
    return user ? user.email : `User ${customerId}`;
  }

  ngOnInit(){
    this.pageTitleService.setTitle('Přehled');
    if (this.keycloakService.hasAdminAccess){
      this.loadAllInvoices();
      this.loadUsers();
    } else {
      this.loadInvoicesForUser();
    }
  }

  loadAllInvoices(): void {
    this.loading.set(true);
    this.error = undefined;
    this.invoicesService.getInvoices(0, 999).subscribe({
      next: (resp: PagedModelInvoice) => {
        const invoices = resp.content;
        if(invoices){
          this.invoices.set(invoices);
        } else {
          this.invoices.set([]);
        }
        this.applyFilter();
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
    const userId = this.keycloakService.currentCustomerId;
    if (!userId){
      console.log('user ID missing!');
      this.error = 'Error - user data missing';
      this.loading.set(false);
      return;
    }
    this.invoicesService.getMyInvoices(0, 999).subscribe({
      next: (resp: PagedModelInvoice) => {
        const invoices = resp.content;
        if(invoices){
          this.invoices.set(invoices);
          console.log(`setting invoices! ${this.invoices()}`);
        } else {
          this.invoices.set([]);
        }
        this.applyFilter();
        this.loading.set(false);
      },
      error: (err) => {
        this.error = err.message || 'Nepodařilo se načíst faktury';
        this.loading.set(false);
      }
    });
  }

  async loadUsers(){
    const usersFromDb = await firstValueFrom(this.customerService.getCustomers(0,999));
    if(usersFromDb.content){
      usersFromDb.content.forEach(user=>{
        this.users.push({email: user.email, id: user.id!});
      })
    }
  }

  applyFilter() {
    const { from, to, customerId, status, amountFrom, amountTo, currency } =
      this.filterForm.value;

    const filtered = this.invoices().filter(inv => {

      if (from && new Date(inv.issueDate) < new Date(from)) return false;
      if (to && new Date(inv.dueDate) > new Date(to)) return false;

      if (customerId && inv.customerId !== Number(customerId)) return false;

      if (status && inv.status !== status) return false;

      if (amountFrom !== null && inv.amount < amountFrom) return false;
      if (amountTo !== null && inv.amount > amountTo) return false;

      return !(currency && inv.currency !== currency);

    });
    this.filteredInvoices.set(filtered);
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
export function getCurrencyColor(currency: string | null | undefined): string {
  if (!currency) return '#b0bec5';
  const colors: Record<string, string> = {
    USD: '#4caf50',
    EUR: '#2196f3',
    CZK: '#ffc107',
  };
  return colors[currency.toUpperCase()];
}

export function hashIdToColor(id: string | number | null | undefined): string {
  if (id === null || id === undefined) return '#b0bec5';

  const str = id.toString();

  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
    hash = hash & hash;
  }

  hash = Math.imul(hash, 2654435761);

  const r = 128 + ((hash >> 24) & 0xFF) % 128;
  const g = 128 + ((hash >> 16) & 0xFF) % 128;
  const b = 128 + ((hash >> 8) & 0xFF) % 128;

  return `#${r.toString(16).padStart(2,'0')}${g.toString(16).padStart(2,'0')}${b.toString(16).padStart(2,'0')}`;
}

