# Wallet components

**Owner: Yaseen Kannemeyer (240453182)**

Components used only by /wallet and /purchases live here.

| File | |
|---|---|
| `TopUpForm.tsx` | Starts a PayFast top-up |
| `SendMoneyForm.tsx` | Sends money to another student by email |
| `ConfirmSendModal.tsx` | The confirm-then-"Money sent" popup for a transfer |
| `money.ts` | `formatZar` and amount validation |

## Two rules

**Never do arithmetic on money.** Amounts arrive as JSON numbers (Jackson
serialises the backend's `BigDecimal` that way) and `money.ts` only formats them.
All money maths belongs on the server, where it stays exact in `BigDecimal`.

**`available` is already net of escrow.** Money leaves the buyer's wallet the
moment they buy, so show "Available · In escrow · Total" and never subtract
`held` from `available` — that deducts it twice.

`TopUpForm` posts the PayFast response as a hidden self-submitting form rather
than building a redirect URL, because the signature covers the field values and
re-encoding anything gets the payment rejected. The form is built with
`document.createElement`, not JSX: a JSX form rendered after `setState` may not
be committed yet when you submit it, and a null ref submits nothing silently —
the button just spins. Nothing it does credits the
wallet: only PayFast's server-to-server callback moves money, which is why
`WalletPage` polls the balance afterwards instead of assuming success.
