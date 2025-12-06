import {Component, inject, OnInit, signal} from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
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
import {SuppliersServiceController} from '../../../suppliers/controller/suppliers.service';
import {ResponsiveService} from '../../../../controller/common.service';
import {NgClass} from '@angular/common';
import { PageTitleService } from '../../../../services/page-title.service';

@Component({
  selector: 'app-invoice-create',
  templateUrl: './invoice-create-edit.component.html',
  styleUrls: ['./invoice-create-edit.component.css'],
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
    NgClass,
  ],
  providers: [
    provideNativeDateAdapter(),
  ]
})
export class InvoiceCreateEditComponent implements OnInit {
  private readonly keycloakService = inject(KeycloakService);
  private readonly customerService = inject(CustomersServiceController);
  private readonly supplierService = inject(SuppliersServiceController);
  protected readonly responsiveService = inject(ResponsiveService);
  protected readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  protected editMode = signal(false);
  protected users: {email: string, id: number}[] = [];
  protected suppliers: {tradeName: string, id: number}[] = [];
  protected editInvoiceId: number = 0;
  protected form!: FormGroup;
  protected submitting = false;
  protected error?: string;
  protected success?: string;
  protected lastCurrency = 'CZK';
  protected currencyOptions = Object.values(CurrencyOptions);
  protected statusOptions = Object.values(InvoiceStatus);
  private readonly pageTitleService = inject(PageTitleService);

  constructor(
    private fb: FormBuilder,
    private invoicesService: InvoicesServiceController,
    private exchangeRates: ExchangeRatesController
  ) {}

  ngOnInit() {
    this.pageTitleService.setTitle('Příprava faktury');
    this.buildForm();
    this.loadData();
  }

  private async loadData() {
    if(this.keycloakService.hasAdminAccess){
      await this.loadUsersInfo();
      await this.loadSuppliersInfo();
    }

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.editMode.set(true);
      this.editInvoiceId = +idParam;
      console.log('editing existing invoice');

      try {
        const invoice = await firstValueFrom(this.invoicesService.getInvoice(+idParam));
        console.log(invoice);

        this.lastCurrency = invoice.currency;

        this.form.patchValue({
          invoiceNumber: invoice.invoiceNumber,
          referenceNumber: invoice.referenceNumber,
          customerId: invoice.customerId,
          supplierId: invoice.supplierId,
          issueDate: invoice.issueDate,
          dueDate: invoice.dueDate,
          amount: invoice.amount,
          subAmount: invoice.items.reduce((a, i) => a + i.totalPrice, 0),
          currency: invoice.currency,
          status: invoice.status
        });

        this.items.clear();

        invoice.items.forEach(item => {
          const group = this.newItem();
          group.patchValue({
            description: item.description,
            name: item.name,
            quantity: item.quantity,
            totalPrice: item.totalPrice,
            unit: item.unit,
            unitPrice: item.unitPrice,
            vat: item.vatRate
          });

          this.items.push(group);
        });

        this.setupCurrencyListener();

      } catch (e) {
        console.error('Error loading invoice', e);
        this.error = 'Nepodařilo se načíst fakturu.';
      }
    } else {
      this.editMode.set(false);
      this.addItem();
      this.lastCurrency = this.form.get('currency')!.value;
      this.setupCurrencyListener();
    }
  }

  private setupCurrencyListener() {
    this.form.get('currency')!.valueChanges.pipe(distinctUntilChanged()).subscribe(newCur => {
      if (!newCur || newCur === this.lastCurrency) return;
      this.convertAllItemsCurrency(this.lastCurrency, newCur);
    });
  }

  private buildForm() {
    const today = new Date().toISOString().substring(0,10);

    this.form = this.fb.group({
      invoiceNumber: ['', [Validators.required, Validators.minLength(3)]],
      referenceNumber: [''],
      customerId: [null, [Validators.required]],
      supplierId: [null, [Validators.required]],
      issueDate: [today, Validators.required],
      dueDate: [today, Validators.required],
      amount: [0, [Validators.required, Validators.min(0)]],
      subAmount: [0],
      currency: ['CZK', Validators.required],
      status: ['DRAFT', Validators.required],
      items: this.fb.array([])
    });
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
    if(this.editMode()){
      const request: InvoiceApi.InvoiceUpdateRequest = {
        ...formValue,
        issueDate: new Date(formValue.issueDate).toISOString().split('T')[0],
        dueDate: new Date(formValue.dueDate).toISOString().split('T')[0],
        amount: formValue.amount,
        items: formValue.items,
        customerId: formValue.customerId,
        supplierId: formValue.supplierId
      };

      console.log('Update request:', request);
      this.invoicesService.updateInvoice(this.editInvoiceId, request).subscribe({
        next: () => {
          this.submitting = false;
          this.success = 'Faktura byla upravena';
          setTimeout(() => this.router.navigate(['/invoices']), 800);
        },
        error: err => {
          this.submitting = false;
          this.error = err.message || 'Chyba při editaci faktury';
        }
      });
    } else {
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
  }

  async loadUsersInfo(){
    const usersFromDb = await firstValueFrom(this.customerService.getCustomers(0,999));
    if(usersFromDb.content){
      usersFromDb.content.forEach(user=>{
        this.users.push({email: user.email, id: user.id!});
      })
    }
  }
  async loadSuppliersInfo(){
    const suppliers = await firstValueFrom(this.supplierService.getSuppliers(0,999));
    if(suppliers.content){
      suppliers.content.forEach(supplier=>{
        this.suppliers.push({tradeName: supplier.tradeName, id: supplier.id!});
      })
    }
  }
}
