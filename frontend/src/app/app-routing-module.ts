import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { CustomersPageComponent } from './features/customers/view/customers-page.component';
import { HomePageComponent } from './features/home/view/home-page.component';
import { InvoiceCreateEditComponent } from './features/invoices/components/invoice-create-edit/invoice-create-edit.component';
import { CustomerCreateComponent } from './features/customers/components/customer-create/customer-create.component';
import {InvoiceDetailComponent} from './features/invoices/components/invoice-detail/invoice-detail.component';
import {
  InvoicePdfComponent
} from './features/invoices/components/invoice-pdf/invoice-pdf-component/invoice-pdf-component';
import {PaymentsComponent} from './features/payments/payments-view/payments/payments-component';
import {InvoicesTableComponent} from './features/invoices/components/invoices-table/invoices-table.component';
import {MockApprovalComponent} from './features/payments/mock-approval/mock-approval.component';
import { SuppliersPageComponent } from './features/suppliers/view/suppliers-page.component';
import { SupplierCreateComponent } from './features/suppliers/components/supplier-create/supplier-create.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', component: HomePageComponent, canActivate: [AuthGuard] },
  { path: 'invoices', component: InvoicesTableComponent, canActivate: [AuthGuard] },
  { path: 'invoices/new', component: InvoiceCreateEditComponent, canActivate: [AuthGuard], data: { roles: ['manager', 'accountant'] } },
  { path: 'invoice/edit/:id', component: InvoiceCreateEditComponent, canActivate: [AuthGuard], data: { roles: ['manager', 'accountant'] } },
  { path: 'customers/new', component: CustomerCreateComponent },
  { path: 'customers/edit/:id', component: CustomerCreateComponent },
  { path: 'payments', component: PaymentsComponent },
  { path: 'mock-approval', component: MockApprovalComponent },
  { path: 'customers', component: CustomersPageComponent, canActivate: [AuthGuard] },
  { path: 'suppliers', component: SuppliersPageComponent, canActivate: [AuthGuard], data: { roles: ['manager'] } },
  { path: 'suppliers/new', component: SupplierCreateComponent, canActivate: [AuthGuard], data: { roles: ['manager'] } },
  { path: 'suppliers/edit/:id', component: SupplierCreateComponent, canActivate: [AuthGuard], data: { roles: ['manager'] } },
  { path: 'invoice/:id', component: InvoiceDetailComponent, canActivate: [AuthGuard] },
  { path: 'invoice/pdf/:id', component: InvoicePdfComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: 'home'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
