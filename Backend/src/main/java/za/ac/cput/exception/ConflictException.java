/*
 ConflictException.java

 Raised when a request is well-formed but loses a race against the current
 state of a row - a listing someone else just bought, a transaction that was
 already confirmed or cancelled, a top-up whose ITN has already been applied.

 This is the exception every compare-and-set in the escrow flow throws when its
 UPDATE affects zero rows. It maps to 409 Conflict, deliberately NOT to the 503
 that IllegalStateException carries in this codebase (that one means "the OTP
 email could not be delivered", which is a very different thing to tell a user).

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.exception;

public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Short machine-readable tag, e.g. LISTING_UNAVAILABLE - surfaces as "code" in the error body. */
    public String getCode() {
        return this.code;
    }

}
