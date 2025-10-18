import { Component, OnInit } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InvoicesServiceController } from '../../controller/invoices.service';
import { InvoiceApi } from '../../../../api/generated';
import { ExchangeRatesController } from '../../controller/exchange.service';
import { distinctUntilChanged } from 'rxjs';
import {
  MatCard, MatCardContent, MatCardHeader, MatCardTitle
} from '@angular/material/card';
import { MatFormField, MatLabel, MatInput } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  MatButton, MatIconButton
} from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatOptionModule } from '@angular/material/core';

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
    MatIconButton
  ]
})
export class InvoiceCreateComponent implements OnInit {
  form!: FormGroup;
  submitting = false;
  error?: string;
  success?: string;
  lastCurrency = 'CZK';

  currencyOptions = ['CZK', 'EUR', 'USD'];
  statusOptions: InvoiceApi.InvoiceCreateRequest['status'][] = ['DRAFT','PENDING','PAID','CANCELLED','OVERDUE'];

  constructor(
    private fb: FormBuilder,
    private invoicesService: InvoicesServiceController,
    private router: Router,
    private exchangeRates: ExchangeRatesController
  ) {}

  ngOnInit(): void {
    const today = new Date().toISOString().substring(0, 10);
    const due = today;

    this.form = this.fb.group({
      invoiceNumber: ['', [Validators.required, Validators.minLength(3)]],
      customerName: ['', Validators.required],
      customerEmail: ['', [Validators.required, Validators.email]],
      issueDate: [today, Validators.required],
      dueDate: [due, Validators.required],
      amount: [0, [Validators.required, Validators.min(0)]],
      currency: ['CZK', Validators.required],
      status: ['DRAFT', Validators.required],
      items: this.fb.array([])
    });

    this.addItem();

    this.form.get('currency')!.valueChanges
      .pipe(distinctUntilChanged())
      .subscribe(newCur => {
        if (!newCur || newCur === this.lastCurrency) return;
        this.convertAllItemsCurrency(this.lastCurrency, newCur);
      });
  }

  get items(): FormArray<FormGroup> {
    return this.form.get('items') as FormArray<FormGroup>;
  }

  newItem(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      totalPrice: [{ value: 0, disabled: true }]
    });
  }

  addItem(): void {
    this.items.push(this.newItem());
  }

  removeItem(i: number): void {
    if (this.items.length > 1) this.items.removeAt(i);
  }

  recalcItemTotal(i: number): void {
    const g = this.items.at(i);
    const q = g.get('quantity')?.value || 0;
    const u = g.get('unitPrice')?.value || 0;
    g.get('totalPrice')?.setValue(+((q * u).toFixed(2)));
    this.recalcInvoiceAmount();
  }

  recalcInvoiceAmount(): void {
    const total = this.items.controls.reduce((sum, c) => {
      const v = +((c.get('totalPrice')?.value) || 0);
      return sum + v;
    }, 0);
    this.form.get('amount')?.setValue(+total.toFixed(2));
  }

  convertAllItemsCurrency(from: string, to: string): void {
    if (this.items.length === 0) { this.lastCurrency = to; return; }

    this.exchangeRates.getRate(from, to).subscribe({
      next: factor => {
        this.items.controls.forEach(ctrl => {
          const unit = ctrl.get('unitPrice')?.value || 0;
          const qty = ctrl.get('quantity')?.value || 0;
          const newUnit = +(unit * factor).toFixed(2);
          ctrl.get('unitPrice')?.setValue(newUnit);
          ctrl.get('totalPrice')?.setValue(+(newUnit * qty).toFixed(2));
        });
        this.recalcInvoiceAmount();
        this.lastCurrency = to;
      },
      error: err => {
        this.form.get('currency')?.setValue(from, { emitEvent: false });
        this.error = 'Chyba při převodu měny: ' + (err.message || 'neznámá chyba');
      }
    });
  }

  submit(): void {
    this.error = undefined;
    this.success = undefined;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;

    const rawItems = this.items.getRawValue();

    const payload = {
      ...this.form.value,
      items: rawItems
    };

    this.invoicesService.createInvoice(payload).subscribe({
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
