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
import {MatButton} from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import {MatNativeDateModule, MatOption, provideNativeDateAdapter} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';

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
    MatSelect
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
}
