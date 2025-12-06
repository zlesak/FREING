import { Component, OnInit, inject, signal } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import { Invoice } from '../../../../api/generated/invoice';
import { firstValueFrom } from 'rxjs';
import {MatCard, MatCardActions, MatCardContent} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {CurrencyPipe, DatePipe, NgClass, NgStyle} from '@angular/common';
import {MatDivider} from '@angular/material/divider';
import{MatIcon} from '@angular/material/icon';
import {CustomersServiceController} from '../../../customers/controller/customers.service';
import {SuppliersServiceController} from '../../../suppliers/controller/suppliers.service';
import {InvoicesServiceController} from '../../../../controller/invoices.service';
import {KeycloakService} from '../../../../keycloak.service';
import {InvoiceStatus} from '../../../common/Enums.js';
import {ResponsiveService} from '../../../../controller/common.service';
import { PageTitleService } from '../../../../services/page-title.service';
import {Customer, Supplier} from '../../../../api/generated/customer';
import { InvoiceStatusTranslationService } from '../../../../services/invoice-status-translation.service';

@Component({
  selector: 'app-invoice-detail-component',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    MatCardActions,
    MatButton,
    CurrencyPipe,
    MatDivider,
    DatePipe,
    MatIcon,
    NgClass,
    NgStyle
  ],
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.css']
})
export class InvoiceDetailComponent implements OnInit {
  protected readonly responsiveService = inject(ResponsiveService);
  protected readonly keycloakService = inject(KeycloakService);
  protected readonly statusTranslation = inject(InvoiceStatusTranslationService);
  private readonly invoiceService = inject(InvoicesServiceController);
  private readonly customerService = inject(CustomersServiceController);
  private readonly supplierService = inject(SuppliersServiceController);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected invoiceId: number = 0;
  protected invoiceDetail = signal<Invoice | null>(null);
  protected customerDetail = signal<Customer | null>(null);
  protected supplierDetail = signal<Supplier | null>(null);
  protected error = signal<string | null>(null);
  protected loading = signal<boolean>(false);
  private readonly pageTitleService = inject(PageTitleService);

  ngOnInit(): void {
    this.pageTitleService.setTitle('Detail faktury');
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
      if(!this.keycloakService.hasAdminAccess) {
        await firstValueFrom(this.invoiceService.markRead(id));
      }
      const invoice = await firstValueFrom(this.invoiceService.getInvoice(id));
      this.invoiceDetail.set(invoice);
      console.log('Loaded invoice:', invoice);

      const userId = invoice.customerId;
      const user = await firstValueFrom(this.customerService.getCustomer(userId));
      this.customerDetail.set(user);

      const supplierId = invoice.supplierId;
      const supplier = await firstValueFrom(this.supplierService.getSupplierById(supplierId));
      this.supplierDetail.set(supplier);

    } catch (error: any) {
      console.error('API Call failed:', error);
      this.error.set(error)
    } finally {
      this.loading.set(false);
    }
  }

  loadSupplierData(){

  }

  edit(){
    this.router.navigate([`/invoice/edit/`, this.invoiceId])
  }
  async delete(){
    console.log(`deleting invoice ${this.invoiceId}`);
    try {
      await firstValueFrom(this.invoiceService.deleteInvoice(this.invoiceId));
      this.router.navigate(['/invoices']);
    } catch (error) {
      console.error('Smazání faktury selhalo:', error);
      this.error.set('Smazání faktury selhalo');
    }
  }
  pay(){
    const invoice = this.invoiceDetail();
    if (!invoice) return;

    this.router.navigate(['/payments'], {
      queryParams: {
        invoiceId: invoice.id,
        amount: invoice.amount,
        currency: invoice.currency,
        description: `Platba za fakturu ${invoice.invoiceNumber}`
      }
    });
  }
  generatePDF(){
    this.router.navigate(['/invoice/pdf/', this.invoiceId])
  }

  readonly paymentStatuses = [
    InvoiceStatus.OVERDUE,
    InvoiceStatus.PENDING,
    InvoiceStatus.SENT
  ] as InvoiceStatus[];

  readonly editStatuses = [
    InvoiceStatus.DRAFT
  ] as InvoiceStatus[];

  getTaxBaseTotal() {
    const invoice = this.invoiceDetail();
    if (!invoice) return 0;
    return invoice.items.reduce((total, item) => {
      const taxBase = item.totalPrice / (1 + item.vatRate / 100);
      return total + taxBase;
    }, 0);
  }
  getVatTotal() {
    const invoice = this.invoiceDetail();
    if (!invoice) return 0;
    return invoice.items.reduce((total, item) => {
      const vatAmount = item.totalPrice - (item.totalPrice / (1 + item.vatRate / 100));
      return total + vatAmount;
    }, 0);
  }

  getStatusStyle() {
    const status = this.invoiceDetail()?.status;
    const styles: { [key: string]: any } = {
      [InvoiceStatus.DRAFT]: {
        'background-color': '#e3f2fd',
        'color': '#1565c0'
      },
      [InvoiceStatus.SENT]: {
        'background-color': '#fff3e0',
        'color': '#e65100'
      },
      [InvoiceStatus.PENDING]: {
        'background-color': '#fff9c4',
        'color': '#f57f17'
      },
      [InvoiceStatus.PAID]: {
        'background-color': '#e8f5e9',
        'color': '#2e7d32'
      },
      [InvoiceStatus.OVERDUE]: {
        'background-color': '#ffebee',
        'color': '#c62828'
      },
      [InvoiceStatus.CANCELLED]: {
        'background-color': '#f5f5f5',
        'color': '#616161'
      }
    };
    return status ? styles[status] : {};
  }

  getStatusLabel(status: InvoiceStatus | undefined): string {
    return this.statusTranslation.getStatusLabel(status);
  }
}
