import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCard, MatCardContent, MatCardActions, MatCardHeader, MatCardTitle } from '@angular/material/card';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageTitleService } from '../../common/controller/page-title.service';

interface MockOrderInfo {
  orderId: string;
  invoiceId: number;
  amount: number;
  currency: string;
  description?: string;
  returnUrl?: string;
  cancelUrl?: string;
}

@Component({
  selector: 'app-mock-approval',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCard,
    MatCardContent,
    MatCardActions,
    MatCardHeader,
    MatCardTitle,
    MatFormField,
    MatLabel,
    MatInput,
    MatError,
    MatButton,
    MatIcon,
    MatProgressSpinner
  ],
  templateUrl: './mock-approval.component.html',
  styleUrl: './mock-approval.component.css'
})
export class MockApprovalComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly pageTitleService = inject(PageTitleService);

  protected orderId = signal<string | null>(null);
  protected orderInfo = signal<MockOrderInfo | null>(null);
  protected loading = signal<boolean>(true);
  protected processing = signal<boolean>(false);
  protected error = signal<string | null>(null);

  protected paymentForm: FormGroup;

  constructor() {
    this.paymentForm = this.fb.group({
      cardNumber: ['4532123456789012', [Validators.required, Validators.pattern(/^\d{16}$/)]],
      cardHolder: ['Jan Novák', [Validators.required, Validators.minLength(3)]],
      expiryDate: ['12/25', [Validators.required, Validators.pattern(/^\d{2}\/\d{2}$/)]],
      cvv: ['123', [Validators.required, Validators.pattern(/^\d{3,4}$/)]]
    });
  }

  async ngOnInit(): Promise<void> {
    this.pageTitleService.setTitle('Platbební brána');

    this.route.queryParams.subscribe(params => {
      const orderId = params['orderId'];
      const amount = params['amount'];
      const currency = params['currency'];
      const invoiceId = params['invoiceId'];

      if (!orderId || !amount || !currency || !invoiceId) {
        this.error.set('Chybí povinné parametry platby');
        this.loading.set(false);
        return;
      }

      const orderInfo: MockOrderInfo = {
        orderId: orderId,
        invoiceId: +invoiceId,
        amount: +amount,
        currency: currency,
        description: params['description'] || undefined,
        returnUrl: params['returnUrl'] || undefined,
        cancelUrl: params['cancelUrl'] || undefined
      };

      this.orderId.set(orderId);
      this.orderInfo.set(orderInfo);
      this.loading.set(false);
    });
  }

  async approvePayment(): Promise<void> {
    if (this.paymentForm.invalid) {
      Object.keys(this.paymentForm.controls).forEach(key => {
        this.paymentForm.get(key)?.markAsTouched();
      });
      this.snackBar.open('Vyplňte prosím všechna pole správně', 'OK', { duration: 3000 });
      return;
    }

    const orderInfo = this.orderInfo();
    if (!orderInfo) return;

    this.processing.set(true);

    try {
      await new Promise(resolve => setTimeout(resolve, 1500));

      this.snackBar.open('✅ Platba byla schválena!', 'OK', { duration: 3000 });

      setTimeout(() => {
        if (orderInfo.returnUrl) {
          window.location.href = orderInfo.returnUrl + `&orderId=${this.orderId()}`;
        } else {
          this.router.navigate(['/payments'], {
            queryParams: {
              message: 'approved',
              orderId: this.orderId()
            }
          });
        }
      }, 1000);
    } catch (err: any) {
      console.error('Chyba při schvalování platby:', err);
      this.snackBar.open('Chyba při zpracování platby', 'OK', { duration: 5000 });
    } finally {
      this.processing.set(false);
    }
  }

  cancelPayment(): void {
    const orderInfo = this.orderInfo();
    if (!orderInfo) return;

    this.snackBar.open('Platba byla zrušena', 'OK', { duration: 3000 });

    setTimeout(() => {
      if (orderInfo.cancelUrl) {
        window.location.href = orderInfo.cancelUrl;
      } else {
        this.router.navigate(['/payments'], {
          queryParams: {
            message: 'cancelled'
          }
        });
      }
    }, 500);
  }

  formatCardNumber(): void {
    const control = this.paymentForm.get('cardNumber');
    if (control) {
      let value = control.value.replace(/\s/g, '');
      control.setValue(value, { emitEvent: false });
    }
  }

  formatExpiryDate(): void {
    const control = this.paymentForm.get('expiryDate');
    if (control) {
      let value = control.value.replace(/\D/g, '');
      if (value.length >= 2) {
        value = value.substring(0, 2) + '/' + value.substring(2, 4);
      }
      control.setValue(value, { emitEvent: false });
    }
  }
}

