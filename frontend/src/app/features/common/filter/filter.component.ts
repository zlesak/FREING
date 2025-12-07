import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-common-filter',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatOptionModule,
    MatButtonModule
  ],
  templateUrl: './filter.component.html',
  styleUrl: './filter.component.scss'
})

export class CommonFilterComponent implements OnInit {
  @Input() users: { email: string; id: number }[] = [];
  @Input() statusOptions: string[] = [];
  @Input() currencyOptions: string[] = [];
  @Input() showCustomer: boolean = false;
  @Input() statusTranslation: any;
  @Input() responsiveService: any;
  @Input() initialValues: any = {};
  @Output() filter = new EventEmitter<any>();

  private fb = inject(FormBuilder);
  filterForm: FormGroup;

  constructor() {
    this.filterForm = this.fb.group({
      from: [null],
      to: [null],
      customerId: [null],
      status: [null],
      amountFrom: [null],
      amountTo: [null],
      currency: [null]
    });
  }

  ngOnInit() {
    this.filterForm.patchValue({
      from: this.initialValues.from ?? null,
      to: this.initialValues.to ?? null,
      customerId: this.initialValues.customerId ?? null,
      status: this.initialValues.status ?? null,
      amountFrom: this.initialValues.amountFrom ?? null,
      amountTo: this.initialValues.amountTo ?? null,
      currency: this.initialValues.currency ?? null
    });
  }

  applyFilter() {
    this.filter.emit(this.filterForm.value);
  }
}
