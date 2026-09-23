/*
 HelperTest.java

 JUnit 5 replacement for the validation section of the old DomainTest main-method
 runner, which used to live (incorrectly) in src/main/java.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class HelperTest {

    @Test
    void isNullOrEmpty() {
        assertTrue(Helper.isNullOrEmpty(null));
        assertTrue(Helper.isNullOrEmpty(""));
        assertTrue(Helper.isNullOrEmpty("   "));
        assertFalse(Helper.isNullOrEmpty("Kannemeyer"));
    }

    @Test
    void isValidEmail() {
        assertTrue(Helper.isValidEmail("student@mycput.ac.za"));
        assertFalse(Helper.isValidEmail("not-an-email"));
        assertFalse(Helper.isValidEmail(null));
    }

    @Test
    void isValidStudentEmail() {
        // Every real team student number must be accepted - if any of these
        // fail, the signup gate is locking out its own developers.
        assertTrue(Helper.isValidStudentEmail("230255639@mycput.ac.za"));
        assertTrue(Helper.isValidStudentEmail("230317693@mycput.ac.za"));
        assertTrue(Helper.isValidStudentEmail("230270565@mycput.ac.za"));
        assertTrue(Helper.isValidStudentEmail("240453182@mycput.ac.za"));
        assertTrue(Helper.isValidStudentEmail("221374698@mycput.ac.za"));

        // The domain is matched case-insensitively, and stray spaces are trimmed.
        assertTrue(Helper.isValidStudentEmail("240453182@MyCPUT.ac.za"));
        assertTrue(Helper.isValidStudentEmail("  240453182@mycput.ac.za  "));

        // 8 and 10 digits are allowed on purpose: CPUT publishes no digit-count
        // spec, so the length stays loose while the domain stays strict.
        assertTrue(Helper.isValidStudentEmail("21021021@mycput.ac.za"));
        assertTrue(Helper.isValidStudentEmail("2102102102@mycput.ac.za"));
    }

    @Test
    void isValidStudentEmailRejectsNonStudents() {
        assertFalse(Helper.isValidStudentEmail("abc@mycput.ac.za"));          // not numeric
        assertFalse(Helper.isValidStudentEmail("12345@mycput.ac.za"));        // too short
        assertFalse(Helper.isValidStudentEmail("24045318200@mycput.ac.za"));  // too long
        assertFalse(Helper.isValidStudentEmail("240453182@gmail.com"));       // public email
        assertFalse(Helper.isValidStudentEmail("240453182@cput.ac.za"));      // staff domain
        assertFalse(Helper.isValidStudentEmail("240453182@mycput.ac.za.evil.com"));
        assertFalse(Helper.isValidStudentEmail("240453182+alias@mycput.ac.za"));
        assertFalse(Helper.isValidStudentEmail(""));
        assertFalse(Helper.isValidStudentEmail(null));
    }

    @Test
    void isValidMobileNumber() {
        assertTrue(Helper.isValidMobileNumber("0821234567"));
        assertFalse(Helper.isValidMobileNumber("082123"));
        assertFalse(Helper.isValidMobileNumber("082-123-4567"));
    }

    @Test
    void isValidPassword() {
        assertTrue(Helper.isValidPassword("password123"));
        assertFalse(Helper.isValidPassword("short"));
        assertFalse(Helper.isValidPassword(null));
    }

    @Test
    void isValidObject() {
        assertTrue(Helper.isValidObject("anything"));
        assertFalse(Helper.isValidObject(null));
    }

    @Test
    void isValidId() {
        assertTrue(Helper.isValidId(1L));
        assertFalse(Helper.isValidId(0L));
        assertFalse(Helper.isValidId(-5L));
    }

    @Test
    void isValidBigDecimal() {
        assertTrue(Helper.isValidBigDecimal(BigDecimal.ZERO));
        assertTrue(Helper.isValidBigDecimal(new BigDecimal("199.99")));
        assertFalse(Helper.isValidBigDecimal(new BigDecimal("-1.00")));
        assertFalse(Helper.isValidBigDecimal(null));
    }

    @Test
    void isValidRating() {
        assertTrue(Helper.isValidRating(1));
        assertTrue(Helper.isValidRating(5));
        assertFalse(Helper.isValidRating(0));
        assertFalse(Helper.isValidRating(6));
    }

    @Test
    void isValidCurrency() {
        assertTrue(Helper.isValidCurrency("ZAR"));
        assertFalse(Helper.isValidCurrency("zar"));
        assertFalse(Helper.isValidCurrency("RANDS"));
    }

    @Test
    void isValidUrl() {
        assertTrue(Helper.isValidUrl("https://uniexchange.co.za/img/1.png"));
        assertFalse(Helper.isValidUrl("not a url"));
        assertFalse(Helper.isValidUrl(null));
    }

}
