import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { InvoicesServiceController } from '../../controller/invoices.service';
import { InvoiceApi } from '../../../../api/generated';
import { ExchangeRatesController } from '../../controller/exchange.service';
import { distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-invoice-create',
  templateUrl: './invoice-create.component.html',
  styleUrls: ['./invoice-create.component.css'],
  standalone: false
})
export class InvoiceCreateComponent implements OnInit {
  form!: FormGroup;
  submitting = false;
  error?: string;
  success?: string;

  currencyOptions = ['CZK', 'EUR', 'USD'];
  statusOptions: InvoiceApi.InvoiceCreateRequest['status'][] = ['DRAFT','PENDING','PAID','CANCELLED','OVERDUE'];

  constructor(
    private fb: FormBuilder,
    private invoicesService: InvoicesServiceController,
    private router: Router,
    private exchangeRates: ExchangeRatesController
  ) {}

  ngOnInit(): void {
    const today = new Date().toISOString().substring(0,10);
    const due = new Date(Date.now()).toISOString().substring(0,10);
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

    this.lastCurrency = this.form.get('currency')!.value;
    this.form.get('currency')!.valueChanges.pipe(distinctUntilChanged()).subscribe(newCur => {
      if (!newCur || newCur === this.lastCurrency) return;
      this.convertAllItemsCurrency(this.lastCurrency, newCur);
    });
  }

  private lastCurrency: string = 'CZK';

  private convertAllItemsCurrency(from: string, to: string): void {
    if (this.items.length === 0) { this.lastCurrency = to; return; }
    this.exchangeRates.getRate(from, to).subscribe({
      next: factor => {
        for (let i = 0; i < this.items.length; i++) {
          const grp = this.items.at(i) as FormGroup;
          const unit = grp.get('unitPrice')?.value || 0;
          const qty = grp.get('quantity')?.value || 0;
          const newUnit = +(unit * factor).toFixed(2);
          grp.get('unitPrice')?.setValue(newUnit);
          const newTotal = +(newUnit * qty).toFixed(2);
            grp.get('totalPrice')?.setValue(newTotal);
        }
        this.recalcInvoiceAmount();
        this.lastCurrency = to;
      },
      error: err => {
        this.form.get('currency')?.setValue(from, { emitEvent: false });
        this.error = 'Chyba při převodu měny: ' + (err.message || 'neznámá chyba');
      }
    });
  }

  get items(): FormArray<FormGroup> { return this.form.get('items') as FormArray<FormGroup>; }

  newItem(): FormGroup {
    return this.fb.group({
      description: this.fb.control<string>('', { validators: [Validators.required], nonNullable: true }),
      quantity: this.fb.control<number>(1, { validators: [Validators.required, Validators.min(1)], nonNullable: true }),
      unitPrice: this.fb.control<number>(0, { validators: [Validators.required, Validators.min(0)], nonNullable: true }),
      totalPrice: this.fb.control<number>({ value: 0, disabled: true } as any)
    });
  }

  addItem(): void {
    this.items.push(this.newItem());
  }

  removeItem(index: number): void {
    if (this.items.length > 1) this.items.removeAt(index);
  }

  recalcItemTotal(index: number): void {
    const group = this.items.at(index) as FormGroup;
    const qty = group.get('quantity')?.value || 0;
    const unit = group.get('unitPrice')?.value || 0;
    const total = +(qty * unit).toFixed(2);
    group.get('totalPrice')?.setValue(total);
    this.recalcInvoiceAmount();
  }

  recalcInvoiceAmount(): void {
    const sum = this.items.controls.reduce((acc, ctrl) => {
      const v = (ctrl as FormGroup).get('totalPrice')?.value || 0;
      return acc + (typeof v === 'number' ? v : parseFloat(v));
    }, 0);
    this.form.get('amount')?.setValue(+sum.toFixed(2));
  }

  submit(): void {
    this.error = undefined; this.success = undefined;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;

    const rawItems: InvoiceApi.InvoiceItemRequest[] = this.items.controls.map(c => {
      const val: any = (c as FormGroup).getRawValue();
      return {
        description: val['description'],
        quantity: val['quantity'],
        unitPrice: val['unitPrice'],
        totalPrice: val['totalPrice']
      } as InvoiceApi.InvoiceItemRequest;
    });

    const payload: InvoiceApi.InvoiceCreateRequest = {
      invoiceNumber: this.form.value.invoiceNumber,
      customerName: this.form.value.customerName,
      customerEmail: this.form.value.customerEmail,
      issueDate: this.form.value.issueDate,
      dueDate: this.form.value.dueDate,
      amount: this.form.value.amount,
      currency: this.form.value.currency,
      status: this.form.value.status,
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
