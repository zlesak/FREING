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

  aresLoading = false;
  aresError?: string;
  aresSuccess?: string;

  constructor(private fb: FormBuilder, private router: Router, private customersService: CustomersServiceController) {
    this.form = this.fb.group({
      name: [''],
      surname: [''],
      tradeName: [''],
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
    }, {
      validators: (group: FormGroup) => {
        const name = group.get('name')?.value?.trim();
        const surname = group.get('surname')?.value?.trim();
        const tradeName = group.get('tradeName')?.value?.trim();
        if ((!name || !surname) && !tradeName) {
          return { nameSurnameOrTradeName: true };
        }
        return null;
      }
    });
  }

  submit() {
    this.error = undefined; this.success = undefined;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    const payload: CreateCustomerDto = {
      name: this.form.value.name,
      surname: this.form.value.surname,
      tradeNumber: this.form.value.tradeName,
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

  fetchFromAres() {
    this.aresError = undefined;
    this.aresSuccess = undefined;

    const ico = (this.form.get('ico')?.value || '').toString().trim();
    if (!ico) {
      this.aresError = 'Zadejte platné IČO';
      return;
    }

    this.aresLoading = true;
    this.customersService.getCustomerInfoFromAres(ico).subscribe({
      next: (resp) => {
        this.aresLoading = false;
        if (!resp) {
          this.aresError = 'Pro zadané IČO nebyly nalezeny žádné údaje';
          return;
        }

        const patch: any = {};
        if (resp.name !== undefined && resp.name !== null) patch.name = resp.name;
        if (resp.surname !== undefined && resp.surname !== null) patch.surname = resp.surname;
        if (resp.tradeName !== undefined && resp.tradeName !== null) patch.tradeName = resp.tradeName;
        if (resp.email !== undefined && resp.email !== null) patch.email = resp.email;
        if (resp.phoneNumber !== undefined && resp.phoneNumber !== null) patch.phoneNumber = resp.phoneNumber;
        if (resp.birthDate !== undefined && resp.birthDate !== null) {
          try {
            const d = new Date(resp.birthDate as any);
            if (!isNaN(d.getTime())) {
              const yyyy = d.getFullYear();
              const mm = String(d.getMonth() + 1).padStart(2, '0');
              const dd = String(d.getDate()).padStart(2, '0');
              patch.birthDate = `${yyyy}-${mm}-${dd}`;
            }
          } catch (e) { /* ignore převod */ }
        }
        if (resp.street !== undefined && resp.street !== null) patch.street = resp.street;
        if (resp.houseNumber !== undefined && resp.houseNumber !== null) patch.houseNumber = resp.houseNumber;
        if (resp.city !== undefined && resp.city !== null) patch.city = resp.city;
        if (resp.zip !== undefined && resp.zip !== null) patch.zip = resp.zip;
        if (resp.country !== undefined && resp.country !== null) patch.country = resp.country;
        if (resp.ico !== undefined && resp.ico !== null) patch.ico = resp.ico;
        if (resp.dic !== undefined && resp.dic !== null) patch.dic = resp.dic;
        if (resp.bankCode !== undefined && resp.bankCode !== null) patch.bankCode = resp.bankCode;
        if (resp.bankAccount !== undefined && resp.bankAccount !== null) patch.bankAccount = resp.bankAccount;
        if (resp.currency !== undefined && resp.currency !== null) patch.currency = resp.currency;

        this.form.patchValue(patch);
        this.aresSuccess = 'Údaje načteny z ARES';
      },
      error: (err) => {
        this.aresLoading = false;
        this.aresError = err?.message || 'Chyba při načítání z ARES';
      }
    });
  }
}
