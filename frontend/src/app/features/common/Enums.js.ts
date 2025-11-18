export const InvoiceStatus = {
  DRAFT: 'DRAFT',
  SENT: 'SENT',
  PENDING: 'PENDING',
  PAID: 'PAID',
  CANCELLED: 'CANCELLED',
  OVERDUE: 'OVERDUE',
  PAID_OVERDUE: 'PAID_OVERDUE',
} as const;

export type InvoiceStatus = typeof InvoiceStatus[keyof typeof InvoiceStatus];


export const CurrencyOptions = {
  CZK: 'CZK',
  EUR: 'EUR',
  USD: 'USD',
} as const;

export type CurrencyOptions = typeof CurrencyOptions[keyof typeof CurrencyOptions]
