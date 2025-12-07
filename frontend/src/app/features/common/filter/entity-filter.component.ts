import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';

@Component({
  selector: 'app-entity-filter',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatOptionModule
  ],
  templateUrl: './entity-filter.component.html',
  styleUrl: './entity-filter.component.scss'
})
export class EntityFilterComponent implements OnInit {
  @Input() showTradeName = true;
  @Input() showName = true;
  @Input() showSurname = true;
  @Input() showEmail = true;
  @Input() showPhone = true;
  @Input() showCity = true;
  @Input() showIco = true;
  @Input() showDic = true;
  @Input() showCountry = true;
  @Input() showCurrency = false;
  @Input() currencyOptions: string[] = [];
  @Input() responsiveService: any;
  @Output() filter = new EventEmitter<any>();

  private fb = inject(FormBuilder);
  filterForm: FormGroup;

  constructor() {
    this.filterForm = this.fb.group({
      tradeName: [''],
      name: [''],
      surname: [''],
      email: [''],
      phoneNumber: [''],
      city: [''],
      ico: [''],
      dic: [''],
      country: [''],
      currency: ['']
    });
  }

  ngOnInit() {}

  applyFilter() {
    this.filter.emit(this.filterForm.value);
  }
}
