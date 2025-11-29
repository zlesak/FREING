import {Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {firstValueFrom} from 'rxjs';
import {PaymentServiceController} from '../../../../../controller/payment.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {ResponsiveService} from '../../../../../controller/common.service';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-invoice-pdf-component',
  imports: [
    NgClass
  ],
  templateUrl: './invoice-pdf-component.html',
  styleUrl: './invoice-pdf-component.css'
})
export class InvoicePdfComponent implements OnInit{
  private readonly paymentService = inject(PaymentServiceController);
  protected readonly responsiveService = inject(ResponsiveService);
  private readonly route = inject(ActivatedRoute);
  private readonly sanitizer = inject(DomSanitizer);
  protected error = signal<string | null>(null);
  protected loading = signal<boolean>(false);
  protected invoiceId!: number;
  protected invoicePdfUrl: SafeResourceUrl | null = null;

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
    await this.loadInvoicePDF(this.invoiceId);
  }

  async loadInvoicePDF(id: number){
   const invoicePdf = await firstValueFrom(this.paymentService.getInvoicePdfAsBlob(id));
   console.log(invoicePdf);
    const url = URL.createObjectURL(invoicePdf);
    this.invoicePdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }


}
