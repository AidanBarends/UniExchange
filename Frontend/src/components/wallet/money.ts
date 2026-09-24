/*
  Money formatting.

  Amounts cross the wire as strings, not numbers, and stay that way until they
  are displayed. BigDecimal on the backend is exact; JavaScript's number is a
  float, so parsing "0.1" and "0.2" and adding them gives 0.30000000000000004.
  Arithmetic on money belongs on the server - this module only renders.
*/

export function formatZar(amount: string | number): string {
  const value = typeof amount === 'number' ? amount : Number(amount)
  if (Number.isNaN(value)) return 'R0.00'

  return new Intl.NumberFormat('en-ZA', {
    style: 'currency',
    currency: 'ZAR',
    minimumFractionDigits: 2,
  }).format(value)
}

/** True when a string amount is a positive value with at most 2 decimals - matches the backend rule. */
export function isValidAmount(raw: string): boolean {
  if (!/^\d+(\.\d{1,2})?$/.test(raw.trim())) return false
  return Number(raw) > 0
}
