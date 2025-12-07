import { Component, EventEmitter, Output, signal, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MatOptionModule } from '@angular/material/core';
import { InvoiceApi } from '../../../api/generated';

@Component({
  selector: 'app-invoice-report-filter',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatOptionModule
  ],
  templateUrl: './invoice-report-filter.component.html',
  styleUrls: ['./invoice-report-filter.component.scss']
})
export class InvoiceReportFilterComponent {
  @Output() generate = new EventEmitter<InvoiceApi.InvoiceReportRequest>();
  @Input() responsiveService: any;
  @Input() users: { email: string; id: number }[] = [];

  protected generating = signal<boolean>(false);
  protected filterForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.filterForm = this.fb.group({
      customerId: [null],
      invoiceNumber: [null],
      referenceNumber: [null],
      issueDateFrom: [null],
      issueDateTo: [null],
      dueDateFrom: [null],
      dueDateTo: [null],
      minAmount: [null],
      maxAmount: [null],
      currency: [null],
      status: [null]
    });
  }

  generateReport(): void {
    if (this.filterForm.valid) {
      const formValue = this.filterForm.value;

      const request: InvoiceApi.InvoiceReportRequest = {
        customerId: formValue.customerId || undefined,
        invoiceNumber: formValue.invoiceNumber || undefined,
        referenceNumber: formValue.referenceNumber || undefined,
        issueDateFrom: formValue.issueDateFrom ? this.formatDate(formValue.issueDateFrom) : undefined,
        issueDateTo: formValue.issueDateTo ? this.formatDate(formValue.issueDateTo) : undefined,
        dueDateFrom: formValue.dueDateFrom ? this.formatDate(formValue.dueDateFrom) : undefined,
        dueDateTo: formValue.dueDateTo ? this.formatDate(formValue.dueDateTo) : undefined,
        minAmount: formValue.minAmount || undefined,
        maxAmount: formValue.maxAmount || undefined,
        currency: formValue.currency || undefined,
        status: formValue.status || undefined
      };

      this.generating.set(true);
      this.generate.emit(request);
    }
  }

  resetFilter(): void {
    this.filterForm.reset();
  }

  setGenerating(value: boolean): void {
    this.generating.set(value);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
