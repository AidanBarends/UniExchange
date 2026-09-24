# Review components

**Owner: Yaseen Kannemeyer (240453182)**

| File | |
|---|---|
| `StarRating.tsx` | Stars, for both display and input |
| `ReviewForm.tsx` | Rate the other party to a completed transaction |

`ReviewForm` deliberately does **not** send who is being rated — the backend
derives that from the transaction, so there is nothing for a client to forge.
That is what makes the ratings, and the Trusted Seller badge built on them,
worth displaying.

`StarRating` is a real radio group when interactive, so it works with a keyboard;
in display mode it collapses to one labelled `role="img"` rather than announcing
five separate stars.
