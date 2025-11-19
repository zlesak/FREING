import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { CustomersServiceController } from '../../controller/customers.service';
import {Router, RouterLink} from '@angular/router';
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
export class CustomerCreateComponent {
  form: FormGroup;
  submitting = false;
  error?: string;
  success?: string;

  constructor(
    private fb: FormBuilder,
    private customersService: CustomersServiceController,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: [''],
      birthDate: [null],
      street: [''],
      houseNumber: [''],
      city: [''],
      zip: [''],
      country: [''],
      ico: [''],
      dic: [''],
      bankCode: [''],
      bankAccount: [''],
      currency: ['CZK'],
    });
  }
  protected defaultBirthday = new Date(1995,1,1);
  protected currency: string = 'CZK';
  protected currencyOptions = ['CZK', 'EUR', 'USD'];
  submit(): void {
    this.error = undefined;
    this.success = undefined;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;

    const request: CustomerApi.CreateCustomerDto = this.form.value;

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
        patchData['name'] = aresInfo.tradeName;
        patchData['surname'] = ' ';
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
