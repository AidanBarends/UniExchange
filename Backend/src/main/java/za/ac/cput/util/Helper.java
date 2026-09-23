/*
 Helper.java

 Helper utility class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.util;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.math.BigDecimal;

public class Helper {

    /*
     A CPUT student address: the student number, then @mycput.ac.za.

     Deliberately 8-10 digits, not exactly 9. Every known team number is 9
     (230255639, 230317693, 230270565, 240453182, 221374698) and CPUT's own
     2012 announcement of the scheme uses a 9-digit example (210210210@...),
     but CPUT publishes no specification of the digit count anywhere. Betting
     the signup gate on a single illustrative example would silently lock
     legitimate students out of the platform - the worst failure this feature
     has - so the length is kept loose while the domain stays strict.

     The real "is this a genuine student" check is not this regex at all: it is
     the emailed OTP. A made-up number in the right shape still cannot register,
     because the code is delivered to a mailbox that does not exist.

     Overridable at runtime via app.auth.student-email-pattern (see
     StudentEmailValidator). Staff (@cput.ac.za) are a separate Entra tenant
     entirely and are not accepted yet; faculty support belongs in a sibling
     isValidStaffEmail once the product needs it.
    */
    public static final String STUDENT_EMAIL_PATTERN = "^\\d{8,10}@mycput\\.ac\\.za$";

    private static final java.util.regex.Pattern STUDENT_EMAIL =
            java.util.regex.Pattern.compile(STUDENT_EMAIL_PATTERN, java.util.regex.Pattern.CASE_INSENSITIVE);

    // Prevent instantiation - utility class
    private Helper() {}

    // Validate a CPUT student email - must be <9 digits>@mycput.ac.za (BR-004)
    public static boolean isValidStudentEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return STUDENT_EMAIL.matcher(email.trim()).matches();
    }

    // Check if a String is null or empty
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Validate email using commons-validator (user.email - BR-004)
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return EmailValidator.getInstance().isValid(email);
    }

    // Validate mobile number - must be digits only and between 10-15 chars (user.cell_phone)
    public static boolean isValidMobileNumber(String mobileNumber) {
        if (isNullOrEmpty(mobileNumber)) return false;
        return mobileNumber.matches("\\d{10,15}");
    }

    // Validate password - must be at least 8 chars (user.password_hash - BR-101)
    public static boolean isValidPassword(String password) {
        if (isNullOrEmpty(password)) return false;
        return password.length() >= 8;
    }

    // Validate object is not null (for enums, entities, dates etc)
    public static boolean isValidObject(Object object) {
        return object != null;
    }

    // Validate positive id - must be > 0 (all *_id / foreign key columns)
    public static boolean isValidId(long id) {
        return id > 0;
    }

    // Validate money - must be non-negative (price, amount, balance -
    // matches chk_listing_price, chk_transaction_amount, chk_payment_amount,
    // chk_wallet_balance)
    public static boolean isValidBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    // Validate rating - must be between 1 and 5 (review.rating - BR-045)
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    // Validate currency - must be 3 uppercase letters (wallet.currency, e.g. ZAR)
    public static boolean isValidCurrency(String currency) {
        if (isNullOrEmpty(currency)) return false;
        return currency.matches("[A-Z]{3}");
    }

    // Validate URL using commons-validator (listing_image.image_url)
    public static boolean isValidUrl(String url) {
        if (isNullOrEmpty(url)) return false;
        return UrlValidator.getInstance().isValid(url);
    }

}