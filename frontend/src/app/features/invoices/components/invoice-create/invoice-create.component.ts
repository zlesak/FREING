import {Component, inject, OnInit} from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InvoicesServiceController } from '../../../../controller/invoices.service';
import { InvoiceApi } from '../../../../api/generated';
import { ExchangeRatesController } from '../../../../controller/exchange.service';
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
import {distinctUntilChanged, firstValueFrom} from 'rxjs';
import {CurrencyOptions, InvoiceStatus} from '../../../common/Enums.js';
import {KeycloakService} from '../../../../keycloak.service';
import {CustomersServiceController} from '../../../customers/controller/customers.service';

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
    provideNativeDateAdapter(),
  ]
})
export class InvoiceCreateComponent implements OnInit {
  private readonly keycloakService = inject(KeycloakService);
  private readonly customerService = inject(CustomersServiceController);
  private readonly router = inject(Router);

  protected users: {email: string, id: number}[] = [];

  form!: FormGroup;
  submitting = false;
  error?: string;
  success?: string;
  lastCurrency = 'CZK';

  currencyOptions = Object.values(CurrencyOptions);
  statusOptions = Object.values(InvoiceStatus);

  constructor(
    private fb: FormBuilder,
    private invoicesService: InvoicesServiceController,
    private exchangeRates: ExchangeRatesController
  ) {}

  async ngOnInit() {
    const today = new Date().toISOString().substring(0,10);
    const due = new Date(Date.now()).toISOString().substring(0,10);
    this.form = this.fb.group({
      invoiceNumber: ['', [Validators.required, Validators.minLength(3)]],
      referenceNumber: [''],
      customerId: [null, [Validators.required]],
      issueDate: [today, Validators.required],
      dueDate: [due, Validators.required],
      amount: [0, [Validators.required, Validators.min(0)]],
      subAmount: [0, [Validators.min(0)]],
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
    if(this.keycloakService.hasAdminAccess){
      await this.loadUsersInfo()
    }
  }

  private convertAllItemsCurrency(from: string, to: string): void {
    if (this.items.length === 0) { this.lastCurrency = to; return; }
    this.exchangeRates.getRate(from, to).subscribe({
      next: factor => {
        for (let i = 0; i < this.items.length; i++) {
          const grp = this.items.at(i) as FormGroup;
          const unit = grp.get('unitPrice')?.value || 0;
          const qty = grp.get('quantity')?.value || 0;
          const vat = grp.get('vat')?.value || 0;
          const newUnit = +(unit * factor).toFixed(2);
          const newSubTotal = +(newUnit * qty).toFixed(2);
          const newTotal = +(newSubTotal * (1 + vat / 100)).toFixed(2);
          grp.get('unitPrice')?.setValue(newUnit);
          grp.get('totalPrice')?.setValue(newTotal);
          grp.get('subTotalPrice')?.setValue(newSubTotal);
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

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  newItem(): FormGroup {
    return this.fb.group({
      name: this.fb.control<string>('', { validators: [Validators.required], nonNullable: true }),
      description: this.fb.control<string>('', { validators: [Validators.required], nonNullable: true }),
      unit: this.fb.control<string>('', { validators: [Validators.required], nonNullable: true }),
      quantity: this.fb.control<number>(1, { validators: [Validators.required, Validators.min(1)], nonNullable: true }),
      unitPrice: this.fb.control<number>(0, { validators: [Validators.required, Validators.min(0)], nonNullable: true }),
      vat: this.fb.control<number>(0, { validators: [Validators.required, Validators.min(0)], nonNullable: true }),
      subTotalPrice: this.fb.control<number>({ value: 0, disabled: true } as any),
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
    const vat = group.get('vat')?.value || 0;
    const subtotal = qty * unit;
    const total = +(subtotal * (1 + vat / 100)).toFixed(2);
    group.get('subTotalPrice')?.setValue(subtotal);
    group.get('totalPrice')?.setValue(total);
    this.recalcInvoiceAmount();
  }

  recalcInvoiceAmount(): void {
    const sum = this.items.controls.reduce((acc, ctrl) => {
      const v = (ctrl as FormGroup).get('totalPrice')?.value || 0;
      return acc + (typeof v === 'number' ? v : parseFloat(v));
    }, 0);
    const subAmount = this.items.controls.reduce((acc, ctrl) => {
      const v = (ctrl as FormGroup).get('subTotalPrice')?.value || 0;
      return acc + (typeof v === 'number' ? v : parseFloat(v));
    }, 0);
    this.form.get('subAmount')?.setValue(+subAmount.toFixed(2));
    this.form.get('amount')?.setValue(+sum.toFixed(2));
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
      amount: formValue.amount,
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
  async loadUsersInfo(){

    //users from keycloak - need to set view users permission for manager and accountant
    const userDetails =  await this.keycloakService.getAllUsers();

    userDetails.forEach(user=> {
      this.users.push({email: user.email, id: user.id});
    })
    console.log(this.users);
    const usersFromDb = await firstValueFrom(this.customerService.getCustomers(0,999));
    console.log(usersFromDb);
    usersFromDb.content.forEach(user=>{
      this.users.push({email: user.email, id: user.id!});
    })
  }
}
