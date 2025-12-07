import {Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {firstValueFrom} from 'rxjs';
import {PaymentServiceController} from '../../../../payments/controller/payment.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {ResponsiveService} from '../../../../common/controller/common.service';
import {NgClass} from '@angular/common';
import {MatProgressBar} from '@angular/material/progress-bar';
import { PageTitleService } from '../../../../common/controller/page-title.service';

@Component({
  selector: 'app-invoice-pdf-component',
  imports: [
    NgClass,
    MatProgressBar
  ],
  templateUrl: './invoice-pdf-component.html',
  styleUrl: './invoice-pdf-component.css'
})
export class InvoicePdfComponent implements OnInit{
  private readonly paymentService = inject(PaymentServiceController);
  private readonly pageTitleService = inject(PageTitleService);
  protected readonly responsiveService = inject(ResponsiveService);
  private readonly route = inject(ActivatedRoute);
  private readonly sanitizer = inject(DomSanitizer);
  protected error = signal<string | null>(null);
  protected loading = signal<boolean>(false);
  protected invoiceId!: number;
  protected invoicePdfUrl: SafeResourceUrl | null = null;

  ngOnInit(): void {
    this.pageTitleService.setTitle('PDF zobrazení faktury');
    this.initLoading();
  }

  async initLoading() {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.loading.set(true);
    if (!idParam) {
      this.error.set('Error - missing invoice ID in URL');
      this.loading.set(false);
      return;
    }

    this.invoiceId = +idParam;

    if (!this.invoiceId) {
      this.error.set('Invalid invoice ID');
      this.loading.set(false);
      return;
    }
    await this.loadInvoicePDF(this.invoiceId);
    this.loading.set(false);
  }

  async loadInvoicePDF(id: number){
    try{
      const invoicePdf = await firstValueFrom(this.paymentService.getInvoicePdfAsBlob(id));
      if(invoicePdf){
        const url = URL.createObjectURL(invoicePdf);
        this.invoicePdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      }
    } catch{
      this.error.set('Error fetching a PDF')
    }
    this.loading.set(false);
  }


}
