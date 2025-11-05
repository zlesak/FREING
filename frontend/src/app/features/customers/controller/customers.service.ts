import { environment } from '../../../../environments/environment';
import { Injectable } from '@angular/core';
import { from, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CustomerApi } from '../../../api/generated';

@Injectable({ providedIn: 'root' })
export class CustomersServiceController {
  constructor() {
    CustomerApi.OpenAPI.BASE = environment.apiBase;
  }

  createCustomer(request: CustomerApi.CreateCustomerDto): Observable<CustomerApi.CustomerDto> {
    return from(CustomerApi.CustomerControllerService.create({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  updateCustomer(request: CustomerApi.CustomerDto): Observable<CustomerApi.CustomerDto> {
    return from(CustomerApi.CustomerControllerService.update({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  getCustomer(id: number): Observable<CustomerApi.CustomerEntity> {
    return from(CustomerApi.CustomerControllerService.getById({ id })).pipe(catchError(this.handleError));
  }

  deleteCustomer(id: number): Observable<void> {
    return from(CustomerApi.CustomerControllerService.delete({ id })).pipe(
      map(() => void 0),
      catchError(this.handleError)
    );
  }

  getCustomers(page = 0, size = 10): Observable<CustomerApi.CustomersPagedResponse> {
    return from(CustomerApi.CustomerControllerService.getAll({ page, size })).pipe(
      catchError(this.handleError)
    );
  }

  private handleError = (err: any) => {
    let message = 'Neznámá chyba';
    if (err && typeof err === 'object') {
      if ('body' in err && err.body && typeof err.body === 'object') {
        message = (err.body as any).message || JSON.stringify(err.body);
      } else if ('message' in err) {
        message = (err as any).message;
      } else if ('status' in err) {
        message = `HTTP ${err.status} ${err.statusText}`;
      }
    }
    return throwError(() => new Error(message));
  };
}
