import {Component, computed, effect, signal} from '@angular/core';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InvoicesServiceController } from '../../controller/invoices.service';
import { InvoiceApi } from '../../../../api/generated';
import { ExchangeRatesController } from '../../controller/exchange.service';
import {
  MatCard, MatCardContent, MatCardHeader
} from '@angular/material/card';
import {MatFormField, MatLabel, MatInput, MatInputModule} from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  MatButton, MatIconButton
} from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {MatNativeDateModule, MatOptionModule} from '@angular/material/core';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDatepickerToggle
} from '@angular/material/datepicker';
import {InvoiceCreateRequest, InvoiceItem} from '../../../../api/generated';

@Component({
  selector: 'app-invoice-create',
  templateUrl: './invoice-create.component.html',
  styleUrls: ['./invoice-create.component.css'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatInput,
    MatSelectModule,
    MatOptionModule,
    MatButton,
    MatIconModule,
    MatIconButton,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatDatepicker,
    MatDatepickerModule,
    MatInputModule,
    MatNativeDateModule,
    ReactiveFormsModule,
    FormsModule,
  ]
})
export class InvoiceCreateComponent {
  submitting = false;
  error?: string;
  success?: string;
  lastCurrency = 'CZK';

  currencyOptions = ['CZK', 'EUR', 'USD'];
  statusOptions: InvoiceApi.InvoiceCreateRequest['status'][] = ['DRAFT','PENDING','PAID','CANCELLED','OVERDUE'];

  protected invoiceNumber = signal <string | null> (null);
  protected customerName = signal <string | null> (null);
  protected customerEmail = signal <string | null> (null);
  protected issueDate = signal <Date | null> (null);
  protected dueDate = signal <Date | null> (null);
  protected currency = signal<string>('CZK');
  protected status = signal <'DRAFT' | 'PENDING' | 'PAID' | 'CANCELLED' | 'OVERDUE'>('DRAFT');
  protected totalAmount = signal <number | null>(null);
  protected items: InvoiceItem[] = [{
    description: '',
    quantity: 1,
    unitPrice: 0,
    totalPrice: 0
  }];

  protected readyToSubmit = computed(() => {
    // Basic required field validation
    const validInvoiceNumber = !!this.invoiceNumber() && this.invoiceNumber()!.trim().length >= 3;
    const validCustomerName = !!this.customerName() && this.customerName()!.trim().length > 0;
    const validEmail =
      typeof this.customerEmail() === 'string' &&
      /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.customerEmail()!);
    const validIssueDate = !!this.issueDate();
    const validDueDate = !!this.dueDate();
    const hasItems =
      this.items.length > 0 &&
      this.items.every(i =>
        i.description.trim().length > 0 &&
        i.quantity > 0 &&
        i.totalPrice > 0
      );


    return (
      validInvoiceNumber &&
      validCustomerName &&
      validEmail &&
      validIssueDate &&
      validDueDate &&
      hasItems &&
      !this.submitting
    );
  });

  constructor(
    private invoicesService: InvoicesServiceController,
    private router: Router,
    private exchangeRates: ExchangeRatesController
  ) {
    effect(()=>{
      const newCurrency = this.currency();
      if(newCurrency === this.lastCurrency) return;
      this.convertAllItemsCurrency(this.lastCurrency, newCurrency);
    })
  }

  newItem(): InvoiceItem{
   return {
     description: '',
     quantity: 1,
     unitPrice: 0,
     totalPrice: 0
   } as InvoiceItem;
  }

  addItem(): void {
    this.items.push(this.newItem());
  }

  removeItem(i: number): void {
    this.items.splice(i,1);
  }

  recalcItemTotal(i: number): void {
    const g = this.items.at(i);
    if(g){
      const q = g.quantity ?? 0;
      const u = g.unitPrice ?? 0;
      g.totalPrice = (+((q * u).toFixed(2)));
      this.recalcInvoiceAmount();
    }
  }

  recalcInvoiceAmount(): void {
    let amount = 0;
    this.items.forEach(item=>{
     amount += item.totalPrice;
   })
    this.totalAmount.set(+amount.toFixed(2));
  }

  convertAllItemsCurrency(from: string, to: string): void {
    if (this.items.length === 0) { this.lastCurrency = to; return; }

    this.exchangeRates.getRate(from, to).subscribe({
      next: factor => {
        this.items.forEach(item => {
          const unitPrice = item.unitPrice ?? 0;
          const quantity = item.quantity ?? 0;
          const newUnit = +(unitPrice * factor).toFixed(2);
          item.unitPrice = (newUnit);
         item.totalPrice = +(newUnit * quantity).toFixed(2);
        });
        this.recalcInvoiceAmount();
        this.lastCurrency = to;
      },
      error: err => {
        this.currency.set(from);
        this.error = 'Chyba při převodu měny: ' + (err.message || 'neznámá chyba');
      }
    });
  }

  submit(): void {
    this.error = undefined;
    this.success = undefined;

    this.submitting = true;

    const request: InvoiceCreateRequest = ({
      invoiceNumber: this.invoiceNumber()!,
      customerName: this.customerName()!,
      customerEmail: this.customerEmail()!,
      issueDate: new Date(this.issueDate()!).toISOString().split('T')[0],
      dueDate: new Date(this.dueDate()!).toISOString().split('T')[0],
      amount: this.totalAmount()!,
      currency: this.currency(),
      status: this.status(),
      items: this.items
    })

    this.invoicesService.createInvoice(request).subscribe({
      next: () => {
        this.submitting = false;
        this.success = 'Faktura byla vytvořena';
        setTimeout(() => this.router.navigate(['/invoices']), 800);
      },
      error: err => {
        this.submitting = false;
        this.error = err.message || 'Chyba při vytváření faktury';
      }
    });
  }
}
