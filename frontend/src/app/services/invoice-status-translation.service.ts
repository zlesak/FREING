import { Injectable } from '@angular/core';
import { InvoiceStatus } from '../features/common/Enums.js';

@Injectable({
  providedIn: 'root'
})
export class InvoiceStatusTranslationService {
  private readonly translations: { [key: string]: string } = {
    [InvoiceStatus.DRAFT]: 'Koncept',
    [InvoiceStatus.SENT]: 'Odesláno',
    [InvoiceStatus.PENDING]: 'Čeká na zaplacení',
    [InvoiceStatus.PAID]: 'Zaplaceno',
    [InvoiceStatus.PAID_OVERDUE]: 'Zaplaceno po splatnosti',
    [InvoiceStatus.OVERDUE]: 'Po splatnosti',
    [InvoiceStatus.CANCELLED]: 'Zrušeno'
  };

  getStatusLabel(status: InvoiceStatus | undefined | null): string {
    if (!status) return '';
    return this.translations[status] || status;
  }

  getAllStatuses(): { value: InvoiceStatus; label: string }[] {
    return Object.values(InvoiceStatus).map(status => ({
      value: status,
      label: this.getStatusLabel(status)
    }));
  }
}

