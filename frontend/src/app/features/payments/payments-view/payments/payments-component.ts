import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PageTitleService } from '../../../common/controller/page-title.service';
import { PaymentServiceController } from '../../controller/payment.service';
import { firstValueFrom } from 'rxjs';
import { MatCard, MatCardContent, MatCardActions } from '@angular/material/card';
import { MatButton } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatIcon } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [
    CommonModule,
    MatCard,
    MatCardContent,
    MatCardActions,
    MatButton,
    MatProgressSpinner,
    MatIcon
  ],
  templateUrl: './payments-component.html',
  styleUrl: './payments-component.css'
})
export class PaymentsComponent implements OnInit {
  private readonly paymentService = inject(PaymentServiceController);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly pageTitleService = inject(PageTitleService);

  protected invoiceId = signal<number | null>(null);
  protected invoiceNumber = signal<string>("");
  protected referenceNumber = signal<string>("");
  protected amount = signal<number | null>(null);
  protected currency = signal<string>('CZK');
  protected description = signal<string | null>(null);
  protected loading = signal<boolean>(false);
  protected error = signal<string | null>(null);
  protected paymentCreated = signal<boolean>(false);
  protected approvalUrl = signal<string | null>(null);
  protected orderId = signal<string | null>(null);

  ngOnInit(): void {
    this.pageTitleService.setTitle('Platba faktury');

    // Načtení parametrů z URL
    this.route.queryParams.subscribe(params => {
      if (params['invoiceId']) {
        this.invoiceId.set(+params['invoiceId']);
      }
      if (params['invoiceNumber']) {
        this.invoiceNumber.set(params['invoiceNumber']);
      }
      if (params['referenceNumber']) {
        this.referenceNumber.set(params['referenceNumber']);
      }
      if (params['amount']) {
        this.amount.set(+params['amount']);
      }
      if (params['currency']) {
        this.currency.set(params['currency']);
      }
      if (params['description']) {
        this.description.set(params['description']);
      }

      if (params['message'] === 'approved' && params['orderId']) {
        this.handleApprovalReturn(params['orderId']);
      } else if (params['message'] === 'cancelled') {
        this.snackBar.open('Platba byla zrušena', 'OK', { duration: 5000 });
      }
    });
  }

  async createPayment(): Promise<void> {
    const invoiceId = this.invoiceId();
    const amount = this.amount();
    const invoiceNumber = this.invoiceNumber();
    const referenceNumber = this.referenceNumber();

    if (!invoiceId || !amount) {
      this.error.set('Chybí povinné údaje pro vytvoření platby');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    try {
      const currentUrl = window.location.origin + window.location.pathname;
      const response = await firstValueFrom(
        this.paymentService.createPayment({
          invoiceId: invoiceId,
          invoiceNumber: invoiceNumber,
          referenceNumber: referenceNumber,
          amount: amount,
          currency: this.currency(),
          description: this.description() || `Platba za fakturu #${invoiceNumber}`,
          returnUrl: `${currentUrl}?message=approved&invoiceId=${invoiceId}`,
          cancelUrl: `${currentUrl}?message=cancelled&invoiceId=${invoiceId}`
        })
      );

      this.paymentCreated.set(true);
      this.approvalUrl.set(response.approvalUrl || null);
      this.orderId.set(response.orderId);

      this.snackBar.open('Platba byla vytvořena', 'OK', { duration: 3000 });

      if (response.approvalUrl) {
        setTimeout(() => {
          if (response.approvalUrl!.startsWith('/')) {
            const url = new URL(response.approvalUrl!, window.location.origin);
            this.router.navigate([url.pathname], { queryParams: Object.fromEntries(url.searchParams) });
          } else {
            window.location.href = response.approvalUrl!;
          }
        }, 1000);
      }
    } catch (err: any) {
      console.error('Chyba při vytváření platby:', err);
      this.error.set('Nepodařilo se vytvořit platbu: ' + (err.message || 'Neznámá chyba'));
      this.snackBar.open('Chyba při vytváření platby', 'ERROR', { duration: 5000 });
    } finally {
      this.loading.set(false);
    }
  }

  async handleApprovalReturn(orderId: string): Promise<void> {
    this.loading.set(true);
    this.orderId.set(orderId);

    try {
      const response = await firstValueFrom(
        this.paymentService.capturePayment({ orderId: orderId })
      );

      this.snackBar.open('Platba byla úspěšně dokončena!', 'OK', { duration: 5000 });

      // Přesměrování zpět na faktury nebo detail faktury
      const invoiceId = this.invoiceId();
      if (invoiceId) {
        setTimeout(() => {
          this.router.navigate(['/invoice', invoiceId]);
        }, 500);
      } else {
        setTimeout(() => {
          this.router.navigate(['/invoices']);
        }, 500);
      }
    } catch (err: any) {
      console.error('Chyba při dokončování platby:', err);
      this.error.set('Nepodařilo se dokončit platbu: ' + (err.message || 'Neznámá chyba'));
      this.snackBar.open('Chyba při dokončování platby', 'ERROR', { duration: 5000 });
    } finally {
      this.loading.set(false);
    }
  }

  goBack(): void {
    const invoiceId = this.invoiceId();
    if (invoiceId) {
      this.router.navigate(['/invoice', invoiceId]);
    } else {
      this.router.navigate(['/invoices']);
    }
  }
}
