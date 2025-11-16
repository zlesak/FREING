import {Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-invoice-pdf-component',
  imports: [],
  templateUrl: './invoice-pdf-component.html',
  styleUrl: './invoice-pdf-component.css'
})
export class InvoicePdfComponent implements OnInit{
  private readonly route = inject(ActivatedRoute);
  protected error = signal<string | null>(null);
  protected loading = signal<boolean>(false);
  protected invoiceId!: number;

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

  loadInvoicePDF(id: number){
    //TODO: load rendered pdf and display
  }


}
