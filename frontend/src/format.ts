export const money = (amount: number, currency: string) =>
  new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(amount)

export const dateTime = (iso: string | null) => (iso ? new Date(iso).toLocaleString() : '—')
