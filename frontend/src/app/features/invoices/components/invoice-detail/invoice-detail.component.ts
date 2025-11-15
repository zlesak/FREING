import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-invoice-detail-component',
  standalone: true,
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.css']
})
export class InvoiceDetailComponent {
  invoiceId!: string;

  constructor(private route: ActivatedRoute) {
    this.invoiceId = this.route.snapshot.paramMap.get('id') ?? '';
  }
}
