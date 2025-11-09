import { Component, OnDestroy } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InvoicesServiceController } from '../../controller/invoices.service';
import { InvoiceApi } from '../../../../api/generated';
import { ExchangeRatesController } from '../../controller/exchange.service';
import {
  MatCard, MatCardContent, MatCardHeader
} from '@angular/material/card';
import { MatFormField, MatLabel, MatInput, MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  MatButton, MatIconButton
} from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatOptionModule, provideNativeDateAdapter } from '@angular/material/core';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDatepickerToggle
} from '@angular/material/datepicker';
import { Subscription } from 'rxjs';

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
  ],
  providers: [
    provideNativeDateAdapter(), // Stejně jako v CustomerCreateComponent
  ]
})
export class InvoiceCreateComponent implements OnDestroy {
  form: FormGroup;
  submitting = false;
  error?: string;
  success?: string;
  lastCurrency = 'CZK';

  currencyOptions = ['CZK', 'EUR', 'USD'];
  statusOptions: InvoiceApi.InvoiceCreateRequest['status'][] = ['DRAFT', 'PENDING', 'PAID', 'CANCELLED', 'OVERDUE'];

  private itemSubscriptions: Subscription[] = [];
  private currencySubscription: Subscription;

  constructor(
    private fb: FormBuilder,
    private invoicesService: InvoicesServiceController,
    private router: Router,
    private exchangeRates: ExchangeRatesController
  ) {
    this.form = this.fb.group({
      invoiceNumber: [null, Validators.required],
      customerName: [null, Validators.required],
      customerEmail: [null, [Validators.required, Validators.email]],
      issueDate: [null, Validators.required],
      dueDate: [null, Validators.required],
      currency: ['CZK', Validators.required],
      status: ['DRAFT', Validators.required],
      totalAmount: [{ value: 0, disabled: true }],

      customerId: [0],
      referenceNumber: [''],

      items: this.fb.array([], Validators.required)
    });

    this.addItem();

    this.currencySubscription = this.form.get('currency')!.valueChanges.subscribe(newCurrency => {
      if (newCurrency && newCurrency !== this.lastCurrency) {
        this.convertAllItemsCurrency(this.lastCurrency, newCurrency);
      }
    });
  }

  ngOnDestroy(): void {
    this.currencySubscription.unsubscribe();
    this.itemSubscriptions.forEach(sub => sub.unsubscribe());
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  newItemFormGroup(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      name: [''],
      unit: [''],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [0, [Validators.required, Validators.min(0.01)]],
      totalPrice: [{ value: 0, disabled: true }],
      vat: [0],
    });
  }

  addItem(): void {
    const itemGroup = this.newItemFormGroup();

    const quantityChanges = itemGroup.get('quantity')!.valueChanges.subscribe(() => {
      this.recalcItemTotal(itemGroup);
    });
    const priceChanges = itemGroup.get('unitPrice')!.valueChanges.subscribe(() => {
      this.recalcItemTotal(itemGroup);
    });

    this.itemSubscriptions.push(quantityChanges);
    this.itemSubscriptions.push(priceChanges);

    this.items.push(itemGroup);
    this.recalcInvoiceAmount();
  }

  removeItem(i: number): void {
    const subIndex = i * 2;
    this.itemSubscriptions[subIndex].unsubscribe();
    this.itemSubscriptions[subIndex + 1].unsubscribe();
    this.itemSubscriptions.splice(subIndex, 2);

    this.items.removeAt(i);
    this.recalcInvoiceAmount();
  }

  recalcItemTotal(itemGroup: FormGroup): void {
    const quantity = itemGroup.get('quantity')?.value ?? 0;
    const unitPrice = itemGroup.get('unitPrice')?.value ?? 0;
    const total = +((quantity * unitPrice).toFixed(2));

    itemGroup.get('totalPrice')?.setValue(total, { emitEvent: false });
    this.recalcInvoiceAmount();
  }

  recalcInvoiceAmount(): void {
    let amount = 0;
    this.items.getRawValue().forEach(item => {
      amount += item.totalPrice;
    });
    this.form.get('totalAmount')?.setValue(+amount.toFixed(2));
  }

  convertAllItemsCurrency(from: string, to: string): void {
    if (this.items.length === 0) {
      this.lastCurrency = to;
      return;
    }

    this.exchangeRates.getRate(from, to).subscribe({
      next: factor => {
        this.items.controls.forEach(control => {
          const itemGroup = control as FormGroup;
          const unitPrice = itemGroup.get('unitPrice')?.value ?? 0;
          const quantity = itemGroup.get('quantity')?.value ?? 0;
          const newUnit = +((unitPrice * factor).toFixed(2));
          const newTotal = +((newUnit * quantity).toFixed(2));

          itemGroup.patchValue({
            unitPrice: newUnit,
            totalPrice: newTotal
          }, { emitEvent: false });
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
      this.items.controls.forEach(control => (control as FormGroup).markAllAsTouched());
      return;
    }

    this.submitting = true;

    const formValue = this.form.getRawValue();

    const request: InvoiceApi.InvoiceCreateRequest = {
      ...formValue,
      issueDate: new Date(formValue.issueDate).toISOString().split('T')[0],
      dueDate: new Date(formValue.dueDate).toISOString().split('T')[0],
      amount: formValue.totalAmount,
      items: formValue.items
    };

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
