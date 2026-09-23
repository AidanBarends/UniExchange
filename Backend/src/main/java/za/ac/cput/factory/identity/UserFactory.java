/*
 UserFactory.java

 Factory for User. All construction goes through here so that every
 User is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.identity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.util.Helper;

public class UserFactory {

    // Prevent instantiation - factory class
    private UserFactory() {}

    public static User createUser(String email, String firstName, String middleName, String lastName,
                                  String cellPhone, String passwordHash, LocalDate dateOfBirth,
                                  AccountStatus accountStatus, Long campusId) {
        if (!Helper.isValidEmail(email)) {
            throw new IllegalArgumentException("User: email is not a valid email address");
        }

        if (Helper.isNullOrEmpty(firstName)) {
            throw new IllegalArgumentException("User: firstName is required");
        }

        if (Helper.isNullOrEmpty(lastName)) {
            throw new IllegalArgumentException("User: lastName is required");
        }

        if (!Helper.isNullOrEmpty(cellPhone) && !Helper.isValidMobileNumber(cellPhone)) {
            throw new IllegalArgumentException("User: cellPhone must be 10 to 15 digits");
        }

        if (Helper.isNullOrEmpty(passwordHash)) {
            throw new IllegalArgumentException("User: passwordHash is required");
        }

        if (!Helper.isValidObject(accountStatus)) {
            throw new IllegalArgumentException("User: accountStatus is required");
        }

        if (campusId != null && !Helper.isValidId(campusId)) {
            throw new IllegalArgumentException("User: campusId must be a positive id when supplied");
        }

        LocalDateTime now = LocalDateTime.now();

        return new User.Builder()
                .setEmail(email)
                .setFirstName(firstName)
                .setMiddleName(middleName)
                .setLastName(lastName)
                .setCellPhone(cellPhone)
                .setPasswordHash(passwordHash)
                .setDateOfBirth(dateOfBirth)
                .setAccountStatus(accountStatus)
                .setCampusId(campusId)
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .build();
    }

    public static User updateUser(User existing, String email, String firstName, String middleName,
                                  String lastName, String cellPhone, String passwordHash,
                                  LocalDate dateOfBirth, AccountStatus accountStatus, Long campusId) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("User: existing record is required for an update");
        }

        if (!Helper.isValidEmail(email)) {
            throw new IllegalArgumentException("User: email is not a valid email address");
        }

        if (Helper.isNullOrEmpty(firstName)) {
            throw new IllegalArgumentException("User: firstName is required");
        }

        if (Helper.isNullOrEmpty(lastName)) {
            throw new IllegalArgumentException("User: lastName is required");
        }

        if (!Helper.isNullOrEmpty(cellPhone) && !Helper.isValidMobileNumber(cellPhone)) {
            throw new IllegalArgumentException("User: cellPhone must be 10 to 15 digits");
        }

        if (Helper.isNullOrEmpty(passwordHash)) {
            throw new IllegalArgumentException("User: passwordHash is required");
        }

        if (!Helper.isValidObject(accountStatus)) {
            throw new IllegalArgumentException("User: accountStatus is required");
        }

        if (campusId != null && !Helper.isValidId(campusId)) {
            throw new IllegalArgumentException("User: campusId must be a positive id when supplied");
        }

        return new User.Builder()
                .copy(existing)
                .setEmail(email)
                .setFirstName(firstName)
                .setMiddleName(middleName)
                .setLastName(lastName)
                .setCellPhone(cellPhone)
                .setPasswordHash(passwordHash)
                .setDateOfBirth(dateOfBirth)
                .setAccountStatus(accountStatus)
                .setCampusId(campusId)
                .setUpdatedAt(LocalDateTime.now())
                .build();
    }


    /**
     * Marks a user's email as verified: ACTIVE, with emailVerifiedAt stamped.
     *
     * Exists as its own method because updateUser has no emailVerifiedAt
     * parameter, so activating through it left the column permanently null -
     * the account looked active but carried no record of when, or whether, the
     * mailbox was ever proved.
     */
    public static User verifyEmail(User existing) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("User: existing record is required to verify");
        }

        LocalDateTime now = LocalDateTime.now();
        return new User.Builder()
                .copy(existing)
                .setAccountStatus(AccountStatus.ACTIVE)
                .setEmailVerifiedAt(now)
                .setUpdatedAt(now)
                .build();
    }

}
