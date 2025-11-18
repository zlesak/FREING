import { Component, OnInit, inject, signal } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import { Invoice, InvoicesService } from '../../../../api/generated/invoice';
import { firstValueFrom } from 'rxjs';
import {MatCard, MatCardActions, MatCardContent} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {CurrencyPipe, DatePipe, PercentPipe} from '@angular/common';
import {MatDivider} from '@angular/material/divider';
import{MatIcon} from '@angular/material/icon';
import {CustomerEntity} from '../../../../api/generated/customer';
import {CustomersServiceController} from '../../../customers/controller/customers.service';
import {InvoicesServiceController} from '../../../../controller/invoices.service';
import {KeycloakService} from '../../../../keycloak.service';
import {InvoiceStatus} from '../../../common/Enums.js';

@Component({
  selector: 'app-invoice-detail-component',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    MatCardActions,
    MatButton,
    CurrencyPipe,
    PercentPipe,
    MatDivider,
    DatePipe,
    MatIcon
  ],
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.css']
})
export class InvoiceDetailComponent implements OnInit {
  protected readonly keycloakService = inject(KeycloakService);
  private readonly invoiceService = inject(InvoicesServiceController);
  private readonly customerService = inject(CustomersServiceController);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  invoiceStatus = InvoiceStatus;
  protected invoiceId!: number;
  protected invoiceDetail = signal<Invoice | null>(null);
  protected customerDetail = signal<CustomerEntity | null>(null);
  protected error = signal<string | null>(null);
  protected loading = signal<boolean>(false);

  ngOnInit(): void {
    this.initLoading();
  }

  async initLoading() {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      this.error.set('Error - missing invoice ID in URL');
      return;
    }

    this.invoiceId = +idParam;

    if (!this.invoiceId) {
      this.error.set('Invalid invoice ID');
      return;
    }

    await this.loadInvoiceDetails(this.invoiceId);
  }

  async loadInvoiceDetails(id: number) {
    this.loading.set(true);
    this.error.set(null);

    try {
      const invoice = await firstValueFrom(this.invoiceService.getInvoice(id));
      this.invoiceDetail.set(invoice);
      console.log('Loaded invoice:', invoice);

      const userId = invoice.customerId;
      const user = await firstValueFrom(this.customerService.getCustomer(userId));
      this.customerDetail.set(user);

    } catch (err: any) {
      console.error('API Call failed:', err);
    } finally {
      this.loading.set(false);
    }
  }


  edit(){

  }
  pay(){

  }
  generatePDF(){
    this.router.navigate(['/invoice/pdf/', this.invoiceId])
  }

  readonly restrictedStatuses = [
    InvoiceStatus.OVERDUE,
    InvoiceStatus.PENDING,
  ] as InvoiceStatus[];

}
