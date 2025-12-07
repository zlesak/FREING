import { environment } from '../../../../environments/environment';
import {inject, Injectable} from '@angular/core';
import { from, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CustomerApi } from '../../../api/generated';
import {
  CustomerControllerService,
  CustomerDto,
} from '../../../api/generated/customer';

@Injectable({ providedIn: 'root' })
export class CustomersServiceController {
  private readonly customerControllerService = inject(CustomerControllerService);
  constructor() {
    CustomerApi.OpenAPI.BASE = environment.apiBase;
  }

  createCustomer(request: CustomerApi.CreateCustomerDto): Observable<CustomerDto> {
    return from(this.customerControllerService.createCustomer({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  updateCustomer(request: CustomerApi.CustomerDto): Observable<CustomerApi.CustomerDto> {
    return from(this.customerControllerService.updateCustomer({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  getCustomer(id: number): Observable<CustomerApi.Customer> {
    return from(this.customerControllerService.getCustomerById({ id })).pipe(catchError(this.handleError));
  }

  deleteCustomer(id: number): Observable<void> {
    return from(this.customerControllerService.deleteCustomer({ id })).pipe(
      map(() => void 0),
      catchError(this.handleError)
    );
  }

  getCustomers(page = 0, size = 10, customerIds?: number[]): Observable<CustomerApi.PagedModelCustomerDto> {
    return from(this.customerControllerService.getAllCustomers({ page, size, customerIds })).pipe(
      catchError(this.handleError)
    );
  }

  getCustomerInfoFromAres(ico: string): Observable<CustomerApi.Customer> {
    return from(this.customerControllerService.getCustomerInfoFromAres({ ico })).pipe(
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
