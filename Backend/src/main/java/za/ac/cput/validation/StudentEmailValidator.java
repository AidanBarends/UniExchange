/*
 StudentEmailValidator.java

 Backs @StudentEmail. Compiles the pattern from app.auth.student-email-pattern
 once at startup, falling back to Helper.STUDENT_EMAIL_PATTERN.

 The property exists so that a legitimate student number of an unexpected length
 can be admitted by editing configuration, not by patching and redeploying - a
 verification gate that is one digit too strict locks real students out of the
 platform entirely, which is the worst failure mode this feature has.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.validation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import za.ac.cput.util.Helper;

public class StudentEmailValidator implements ConstraintValidator<StudentEmail, String> {

    private static final Logger log = LoggerFactory.getLogger(StudentEmailValidator.class);

    private final Pattern pattern;

    public StudentEmailValidator(
            @Value("${app.auth.student-email-pattern:}") String configuredPattern) {

        this.pattern = compile(configuredPattern);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Leave "is it present at all" to @NotBlank so the two concerns report separately.
        if (Helper.isNullOrEmpty(email)) {
            return true;
        }
        return this.pattern.matcher(email.trim()).matches();
    }

    private static Pattern compile(String configuredPattern) {
        if (Helper.isNullOrEmpty(configuredPattern)) {
            return Pattern.compile(Helper.STUDENT_EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
        }
        try {
            return Pattern.compile(configuredPattern.trim(), Pattern.CASE_INSENSITIVE);
        }
        catch (PatternSyntaxException ex) {
            // Never let a typo in configuration open the gate to everyone.
            log.error("Invalid app.auth.student-email-pattern '{}' - falling back to the default",
                    configuredPattern, ex);
            return Pattern.compile(Helper.STUDENT_EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
        }
    }

}
