import {Component, OnInit, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { CustomersServiceController } from '../../controller/customers.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import { CustomerApi } from '../../../../api/generated';
import {MatFormField} from '@angular/material/input';
import {MatLabel} from '@angular/material/input';
import {MatInput} from '@angular/material/input';
import {MatCard, MatCardContent, MatCardHeader} from '@angular/material/card';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {MatButton, MatIconButton} from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import {MatNativeDateModule, MatOption, provideNativeDateAdapter} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {firstValueFrom} from 'rxjs';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-customer-create',
  templateUrl: './customer-create.component.html',
  styleUrls: ['./customer-create.component.css'],
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormField,
    MatLabel,
    MatInput,
    MatCardContent,
    MatCardHeader,
    MatCard,
    MatDatepickerToggle,
    MatDatepicker,
    MatDatepickerInput,
    MatFormFieldModule,
    MatButton,
    MatNativeDateModule,
    MatOption,
    MatSelect,
    MatIcon,
    MatIconButton
  ], providers: [
    provideNativeDateAdapter(),
  ],
  standalone: true
})
export class CustomerCreateComponent implements OnInit {
  form: FormGroup;
  submitting = false;
  error?: string;
  success?: string;
  editMode = signal(false);
  editCustomerId: number = 0;

  constructor(
    private fb: FormBuilder,
    private customersService: CustomersServiceController,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      name: [''],
      surname: [''],
      tradeName: [''],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required]],
      birthDate: [null],
      street: ['', [Validators.required]],
      houseNumber: ['', [Validators.required]],
      city: ['', [Validators.required]],
      zip: ['', [Validators.required]],
      country: ['Česká republika', [Validators.required]],
      ico: [''],
      dic: [''],
      bankCode: [''],
      bankAccount: [''],
      currency: ['CZK', [Validators.required]],
    }, { validators: this.nameOrTradeNameValidator });
  }

  private nameOrTradeNameValidator(group: FormGroup): { [key: string]: boolean } | null {
    const name = group.get('name')?.value?.trim();
    const surname = group.get('surname')?.value?.trim();
    const tradeName = group.get('tradeName')?.value?.trim();

    const hasPersonalName = name && surname;
    const hasTradeName = tradeName;

    if (!hasPersonalName && !hasTradeName) {
      return { nameOrTradeNameRequired: true };
    }

    // Nesmí být vyplněno obojí
    if (hasPersonalName && hasTradeName) {
      return { bothNameTypesProvided: true };
    }

    return null;
  }
  protected defaultBirthday = new Date(1995,1,1);
  protected currency: string = 'CZK';
  protected currencyOptions = ['CZK', 'EUR', 'USD'];

  ngOnInit(): void {
    this.editCustomerId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.editCustomerId) {
      this.editMode.set(true);
      this.loadCustomer();
    }
  }

  private loadCustomer(): void {
    this.customersService.getCustomer(this.editCustomerId).subscribe({
      next: (customer: CustomerApi.Customer) => {
        this.form.patchValue({
          name: customer.name || '',
          surname: customer.surname || '',
          tradeName: customer.tradeName || '',
          email: customer.email,
          phoneNumber: customer.phoneNumber,
          birthDate: customer.birthDate ? new Date(customer.birthDate) : null,
          street: customer.street,
          houseNumber: customer.houseNumber,
          city: customer.city,
          zip: customer.zip,
          country: customer.country,
          ico: customer.ico,
          dic: customer.dic,
          bankCode: customer.bankCode,
          bankAccount: customer.bankAccount,
          currency: customer.currency,
        });
      },
      error: (err: any) => {
        this.error = err?.message || 'Nepodařilo se načíst zákazníka';
      },
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

    if (this.editMode()) {
      const formValue = this.form.value;

      const hasPersonalName = formValue.name?.trim() && formValue.surname?.trim();
      const hasTradeName = formValue.tradeName?.trim();

      const updateRequest: CustomerApi.CustomerDto = {
        id: this.editCustomerId,
        name: hasPersonalName ? formValue.name : '',
        surname: hasPersonalName ? formValue.surname : '',
        tradeName: hasTradeName ? formValue.tradeName : '',
        email: formValue.email,
        phoneNumber: formValue.phoneNumber || '',
        birthDate: formValue.birthDate,
        street: formValue.street || '',
        houseNumber: formValue.houseNumber || '',
        city: formValue.city || '',
        zip: formValue.zip || '',
        country: formValue.country || '',
        ico: formValue.ico || '',
        dic: formValue.dic || '',
        bankCode: formValue.bankCode || '',
        bankAccount: formValue.bankAccount || '',
        currency: formValue.currency || 'CZK',
      };

      this.customersService.updateCustomer(updateRequest).subscribe({
        next: () => {
          this.submitting = false;
          this.success = 'Zákazník byl úspěšně aktualizován';
          setTimeout(() => this.router.navigate(['/customers']), 800);
        },
        error: (err) => {
          this.submitting = false;
          this.error = err.message || 'Chyba při aktualizaci zákazníka';
          console.error(err);
        },
      });
    } else {
      const formValue = this.form.value;

      const hasPersonalName = formValue.name?.trim() && formValue.surname?.trim();
      const hasTradeName = formValue.tradeName?.trim();

      const request: CustomerApi.CreateCustomerDto = {
        name: hasPersonalName ? formValue.name : '',
        surname: hasPersonalName ? formValue.surname : '',
        tradeName: hasTradeName ? formValue.tradeName : '',
        email: formValue.email,
        phoneNumber: formValue.phoneNumber || '',
        birthDate: formValue.birthDate,
        street: formValue.street || '',
        houseNumber: formValue.houseNumber || '',
        city: formValue.city || '',
        zip: formValue.zip || '',
        country: formValue.country || '',
        ico: formValue.ico || '',
        dic: formValue.dic || '',
        bankCode: formValue.bankCode || '',
        bankAccount: formValue.bankAccount || '',
        currency: formValue.currency || 'CZK',
      };

      console.log('Creating customer with data:', request);

      this.customersService.createCustomer(request).subscribe({
        next: () => {
          this.submitting = false;
          this.success = 'Zákazník byl úspěšně vytvořen';
          setTimeout(() => this.router.navigate(['/customers']), 800);
        },
        error: (err) => {
          this.submitting = false;
          this.error = err.message || 'Chyba při vytváření zákazníka';
          console.error(err);
        },
      });
    }
  }

  async loadInfoFromARES(): Promise<void> {
    const icoControl = this.form.get('ico');
    let ico:string = icoControl?.value;
    ico = ico.trim();

    if (!ico || ico.length !== 8) {
      console.error('Invalid IČO format.');
      icoControl?.setErrors({ 'invalidIcoFormat': true });
      return;
    }
    try {
      const aresInfo = await firstValueFrom(this.customersService.getCustomerInfoFromAres(ico));

      console.log('ARES Info Received:', aresInfo);

      const patchData: { [key: string]: any } = {};

      const isPresent = (value: any): boolean => {
        return value !== null && value !== undefined && value !== '';
      }

      if (isPresent(aresInfo.tradeName)) {
        patchData['name'] = '';
        patchData['surname'] = '';
        patchData['tradeName'] = aresInfo.tradeName;
      }

      patchData['street'] = aresInfo.street;
      patchData['houseNumber'] = aresInfo.houseNumber;
      patchData['city'] = aresInfo.city;
      patchData['zip'] = aresInfo.zip;
      patchData['country'] = aresInfo.country;

      patchData['ico'] = aresInfo.ico;
      if (isPresent(aresInfo.dic)) {
        patchData['dic'] = aresInfo.dic;
      }

      if (isPresent(aresInfo.bankAccount)) {
        patchData['bankAccount'] = aresInfo.bankAccount;
      }
      if (isPresent(aresInfo.bankCode)) {
        patchData['bankCode'] = aresInfo.bankCode;
      }

      if (isPresent(aresInfo.currency)) {
        patchData['currency'] = aresInfo.currency;
      }
      /* ARES doesnt return correct birthday
      if (isPresent(aresInfo.birthDate)) {
        patchData['birthDate'] = new Date(aresInfo.birthDate!);
      }
    */
      this.form.patchValue(patchData);

    } catch (error) {
      console.error('Error fetching ARES information:', error);
      icoControl?.setErrors({ 'aresLookupFailed': true });
    }
  }

}
