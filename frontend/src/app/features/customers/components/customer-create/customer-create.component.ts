import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CustomersServiceController } from '../../controller/customers.service';
import { CreateCustomerDto } from '../../../../api/generated/customer/models/CreateCustomerDto';
import { Router } from '@angular/router';

@Component({
  selector: 'app-customer-create',
  templateUrl: './customer-create.component.html',
  styleUrls: ['./customer-create.component.css'],
  standalone: false
})
export class CustomerCreateComponent {
  form: FormGroup;
  submitting = false;
  error?: string;
  success?: string;

  constructor(private fb: FormBuilder, private router: Router, private customersService: CustomersServiceController) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      surname: [''],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: [''],
      birthDate: [''],
      street: [''],
      houseNumber: [''],
      city: [''],
      zip: [''],
      country: [''],
      ico: [''],
      dic: [''],
      bankCode: [''],
      bankAccount: [''],
      currency: ['']
    });
  }

  submit() {
    this.error = undefined; this.success = undefined;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    const payload: CreateCustomerDto = {
      name: this.form.value.name,
      surname: this.form.value.surname,
      email: this.form.value.email,
      phoneNumber: this.form.value.phoneNumber,
      birthDate: this.form.value.birthDate,
      street: this.form.value.street,
      houseNumber: this.form.value.houseNumber,
      city: this.form.value.city,
      zip: this.form.value.zip,
      country: this.form.value.country,
      ico: this.form.value.ico,
      dic: this.form.value.dic,
      bankCode: this.form.value.bankCode,
      bankAccount: this.form.value.bankAccount,
      currency: this.form.value.currency
    };

    this.customersService.createCustomer(payload).subscribe({
      next: (resp) => {
        this.submitting = false;
        this.success = 'Zákazník vytvořen: ' + (resp.id ?? '');
        setTimeout(() => this.router.navigate(['/customers']), 800);
      },
      error: (err) => {
        this.submitting = false;
        this.error = err?.message || 'Chyba při vytváření zákazníka';
      }
    });
  }
}
