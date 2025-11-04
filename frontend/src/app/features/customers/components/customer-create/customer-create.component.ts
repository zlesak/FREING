import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CustomerControllerService } from '../../../../api/generated/customer/services/CustomerControllerService';
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

  constructor(private fb: FormBuilder, private router: Router) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  submit() {
    this.error = undefined; this.success = undefined;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    const payload: CreateCustomerDto = {
      name: this.form.value.name,
      surname: '',
      email: this.form.value.email,
      phoneNumber: '',
      birthDate: '',
      street: '',
      houseNumber: '',
      city: '',
      zip: '',
      country: ''
    };

    CustomerControllerService.create({ requestBody: payload }).then(resp => {
      this.submitting = false;
      this.success = 'Zákazník vytvořen: ' + (resp.id ?? '');
      setTimeout(() => this.router.navigate(['/customers']), 800);
    }).catch(err => {
      this.submitting = false;
      this.error = err?.message || 'Chyba při vytváření zákazníka';
    });
  }
}
