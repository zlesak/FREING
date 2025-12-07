import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SuppliersServiceController } from '../../controller/suppliers.service';
import { MatCard, MatCardContent, MatCardHeader } from '@angular/material/card';
import { MatFormField, MatLabel, MatInput, MatInputModule } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CreateSupplierDto, Supplier, SupplierDto } from '../../../../api/generated/customer';
import { CurrencyOptions } from '../../../common/Enums.js';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { PageTitleService } from '../../../../services/page-title.service';

@Component({
  selector: 'app-supplier-create',
  templateUrl: './supplier-create.component.html',
  styleUrls: ['./supplier-create.component.css'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatInput,
    MatButton,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatOptionModule,
  ],
})
export class SupplierCreateComponent implements OnInit {
  private readonly suppliersService = inject(SuppliersServiceController);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly pageTitleService = inject(PageTitleService);

  protected editMode = signal(false);
  protected editSupplierId: number = 0;
  protected form!: FormGroup;
  protected submitting = false;
  protected error?: string;
  protected success?: string;
  protected currencyOptions = Object.values(CurrencyOptions);

  ngOnInit(): void {
    this.initForm();

    this.editSupplierId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.editSupplierId) {
      this.editMode.set(true);
      this.pageTitleService.setTitle('Upravit dodavatele');
      this.loadSupplier();
    } else {
      this.pageTitleService.setTitle('Nový dodavatel');
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      tradeName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required]],
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
    });
  }

  private loadSupplier(): void {
    this.suppliersService.getSupplierById(this.editSupplierId).subscribe({
      next: (supplier: Supplier) => {
        // Naplníme formulář daty z BE
        this.form.patchValue({
          tradeName: supplier.tradeName || '',
          email: supplier.email || '',
          phoneNumber: supplier.phoneNumber || '',
          street: supplier.street || '',
          houseNumber: supplier.houseNumber || '',
          city: supplier.city || '',
          zip: supplier.zip || '',
          country: supplier.country || 'Česká republika',
          ico: supplier.ico || '',
          dic: supplier.dic || '',
          bankCode: supplier.bankCode || '',
          bankAccount: supplier.bankAccount || '',
          currency: supplier.currency || 'CZK',
        });
      },
      error: (err: any) => {
        this.error = err?.message || 'Nepodařilo se načíst dodavatele';
        console.error('Error loading supplier:', err);
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.error = undefined;
    this.success = undefined;

    if (this.editMode()) {
      const formValue = this.form.value;
      const updatedSupplier: SupplierDto = {
        id: this.editSupplierId,
        tradeName: formValue.tradeName,
        email: formValue.email,
        phoneNumber: formValue.phoneNumber,
        street: formValue.street,
        houseNumber: formValue.houseNumber,
        city: formValue.city,
        zip: formValue.zip,
        country: formValue.country,
        ico: formValue.ico || '',
        dic: formValue.dic || '',
        bankCode: formValue.bankCode || '',
        bankAccount: formValue.bankAccount || '',
        currency: formValue.currency || 'CZK',
      };
      this.suppliersService.updateSupplier(updatedSupplier).subscribe({
        next: () => {
          this.success = 'Dodavatel byl úspěšně aktualizován';
          this.submitting = false;
          setTimeout(() => this.router.navigate(['/suppliers']), 1500);
        },
        error: (err) => {
          this.error = err?.message || 'Nepodařilo se aktualizovat dodavatele';
          this.submitting = false;
        },
      });
    } else {
      const newSupplier: CreateSupplierDto = this.form.value;
      this.suppliersService.createSupplier(newSupplier).subscribe({
        next: () => {
          this.success = 'Dodavatel byl úspěšně vytvořen';
          this.submitting = false;
          setTimeout(() => this.router.navigate(['/suppliers']), 1500);
        },
        error: (err) => {
          this.error = err?.message || 'Nepodařilo se vytvořit dodavatele';
          this.submitting = false;
        },
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/suppliers']);
  }
}

