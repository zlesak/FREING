export const InvoiceStatus = {
  DRAFT: 'DRAFT',
  PENDING: 'PENDING',
  PAID: 'PAID',
  CANCELLED: 'CANCELLED',
  OVERDUE: 'OVERDUE',
} as const;

export type InvoiceStatus = typeof InvoiceStatus[keyof typeof InvoiceStatus];

export const CurrencyOptions = {
  CZK: 'CZK',
  EUR: 'EUR',
  USD: 'USD',
} as const;

export type CurrencyOptions = typeof CurrencyOptions[keyof typeof CurrencyOptions]
