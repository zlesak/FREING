import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SupplierControllerService } from '../../../api/generated/customer/services/SupplierControllerService';
import {
  CreateSupplierDto,
  PagedModelSupplierDto,
  Supplier,
  SupplierDto
} from '../../../api/generated/customer';

@Injectable({
  providedIn: 'root',
})
export class SuppliersServiceController {
  private readonly supplierService = inject(SupplierControllerService);


  getSuppliers(params: {
    page?: number,
    size?: number,
    supplierId?: number,
    supplierIds?: number[],
    tradeName?: string,
    email?: string,
    phoneNumber?: string,
    city?: string,
    ico?: string,
    dic?: string,
    country?: string,
    currency?: string
  } = {}): Observable<PagedModelSupplierDto> {
    return this.supplierService.getAllSuppliers(params);
  }

  getSuppliersByIds(supplierIds: number[]): Observable<PagedModelSupplierDto> {
    return this.supplierService.getAllSuppliers({ supplierIds, size: supplierIds.length });
  }

  getSupplierById(id: number): Observable<Supplier> {
    return this.supplierService.getSupplierById({ id });
  }

  createSupplier(supplier: CreateSupplierDto): Observable<SupplierDto> {
    return this.supplierService.createSupplier({ requestBody: supplier });
  }

  updateSupplier(supplier: SupplierDto): Observable<SupplierDto> {
    return this.supplierService.updateSupplier({ requestBody: supplier });
  }

  deleteSupplier(id: number): Observable<any> {
    return this.supplierService.deleteSupplier({ id });
  }
}

