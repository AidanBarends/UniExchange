/*
 StudentEmail.java

 Bean Validation constraint for "this must be a CPUT student address".

 Applied at the registration boundary rather than inside UserFactory on purpose:
 being a student email is a *registration policy*, not a User invariant. The
 product brief also covers faculty and approved vendors, who will legitimately
 hold non-student addresses - baking the rule into the entity's factory would
 block them later and would fight the app.auth.student-email-pattern override.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target({FIELD, METHOD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StudentEmailValidator.class)
public @interface StudentEmail {

    String message() default "Use your CPUT student email, for example 240453182@mycput.ac.za";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
