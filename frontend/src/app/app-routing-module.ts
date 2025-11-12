import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { CustomersPageComponent } from './features/customers/view/customers-page.component';
import { HomePageComponent } from './features/home/view/home-page.component';
import { InvoiceCreateComponent } from './features/invoices/components/invoice-create/invoice-create.component';
import { CustomerCreateComponent } from './features/customers/components/customer-create/customer-create.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', component: HomePageComponent, canActivate: [AuthGuard] },
  { path: 'invoices/new', component: InvoiceCreateComponent, canActivate: [AuthGuard], data: { roles: ['manager', 'accountant'] } },
  { path: 'customers/new', component: CustomerCreateComponent },
  { path: 'customers', component: CustomersPageComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: 'home'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
