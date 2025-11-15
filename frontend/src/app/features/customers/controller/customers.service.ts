import { environment } from '../../../../environments/environment';
import {inject, Injectable} from '@angular/core';
import { from, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CustomerApi } from '../../../api/generated';
import {CustomerControllerService} from '../../../api/generated/customer';

@Injectable({ providedIn: 'root' })
export class CustomersServiceController {
  private readonly customerControllerService = inject(CustomerControllerService);
  constructor() {
    CustomerApi.OpenAPI.BASE = environment.apiBase;
  }

  createCustomer(request: CustomerApi.CreateCustomerDto): Observable<CustomerApi.CustomerDto> {
    return from(this.customerControllerService.create({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  updateCustomer(request: CustomerApi.CustomerDto): Observable<CustomerApi.CustomerDto> {
    return from(this.customerControllerService.update({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  getCustomer(id: number): Observable<CustomerApi.Customer> {
    return from(this.customerControllerService.getById({ id })).pipe(catchError(this.handleError));
  }

  deleteCustomer(id: number): Observable<void> {
    return from(this.customerControllerService.delete({ id })).pipe(
      map(() => void 0),
      catchError(this.handleError)
    );
  }

  getCustomers(page = 0, size = 10): Observable<CustomerApi.PagedModelCustomerDto> {
    return from(this.customerControllerService.getAll({ page, size })).pipe(
      catchError(this.handleError)
    );
  }

  getCustomerInfoFromAres(ico: string): Observable<CustomerApi.Customer> {
    return from(this.customerControllerService.getCustomerInfoFromAresByIco({ ico })).pipe(
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
