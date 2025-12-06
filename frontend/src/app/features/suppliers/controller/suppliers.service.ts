import { environment } from '../../../../environments/environment';
import { inject, Injectable } from '@angular/core';
import { from, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CustomerApi } from '../../../api/generated';
import {
  SupplierControllerService,
  SupplierDto,
  CreateSupplierDto,
  Supplier,
  PagedModelSupplierDto,
} from '../../../api/generated/customer';

@Injectable({ providedIn: 'root' })
export class SuppliersServiceController {
  private readonly supplierControllerService = inject(SupplierControllerService);

  constructor() {
    CustomerApi.OpenAPI.BASE = environment.apiBase;
  }

  createSupplier(request: CreateSupplierDto): Observable<SupplierDto> {
    return from(this.supplierControllerService.createSupplier({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  updateSupplier(request: SupplierDto): Observable<SupplierDto> {
    return from(this.supplierControllerService.updateSupplier({ requestBody: request })).pipe(
      catchError(this.handleError)
    );
  }

  getSupplier(id: number): Observable<Supplier> {
    return from(this.supplierControllerService.getSupplierById({ id })).pipe(
      catchError(this.handleError)
    );
  }

  deleteSupplier(id: number): Observable<void> {
    return from(this.supplierControllerService.deleteSupplier({ id })).pipe(
      map(() => void 0),
      catchError(this.handleError)
    );
  }

  getSuppliers(page = 0, size = 10): Observable<PagedModelSupplierDto> {
    return from(this.supplierControllerService.getAllSuppliers({ page, size })).pipe(
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

