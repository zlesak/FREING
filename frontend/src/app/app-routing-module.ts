import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { InvoicesPageComponent } from './features/invoices/view/invoices-page.component';
import { CustomersPageComponent } from './features/customers/view/customers-page.component';
import { PaymentsPageComponent } from './features/payments/view/payments-page.component';
import { HomePageComponent } from './features/home/view/home-page.component';
import { InvoiceCreateComponent } from './features/invoices/components/invoice-create/invoice-create.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', component: HomePageComponent, canActivate: [AuthGuard] },
  { path: 'invoices', component: InvoicesPageComponent, canActivate: [AuthGuard], data: { roles: ['manager', 'accountant'] } },
  { path: 'invoices/new', component: InvoiceCreateComponent, canActivate: [AuthGuard], data: { roles: ['manager', 'accountant'] } },
  { path: 'customers', component: CustomersPageComponent, canActivate: [AuthGuard] },
  { path: 'payments', component: PaymentsPageComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: 'home'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
