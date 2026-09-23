/*
 User.java

 User POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import za.ac.cput.domain.enums.AccountStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "`user`")
public class User {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 20)
    private String cellPhone;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "account_status")
    private AccountStatus accountStatus;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "campus_id")
    private Long campusId;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    //  Constructors
    protected User() {
        // Required by JPA
    }

    private User(Builder builder) {
        this.userId = builder.userId;
        this.email = builder.email;
        this.firstName = builder.firstName;
        this.middleName = builder.middleName;
        this.lastName = builder.lastName;
        this.cellPhone = builder.cellPhone;
        this.passwordHash = builder.passwordHash;
        this.dateOfBirth = builder.dateOfBirth;
        this.accountStatus = builder.accountStatus;
        this.emailVerifiedAt = builder.emailVerifiedAt;
        this.campusId = builder.campusId;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    //  Getters
    public long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public Long getCampusId() {
        return campusId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", cellPhone='" + cellPhone + '\'' +
                // Never print the hash: this entity is logged in auth flows.
                ", passwordHash='[REDACTED]'" +
                ", dateOfBirth=" + dateOfBirth +
                ", accountStatus=" + accountStatus +
                ", emailVerifiedAt=" + emailVerifiedAt +
                ", campusId=" + campusId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long userId;
        private String email;
        private String firstName;
        private String middleName;
        private String lastName;
        private String cellPhone;
        private String passwordHash;
        private LocalDate dateOfBirth;
        private AccountStatus accountStatus;
        private LocalDateTime emailVerifiedAt;
        private Long campusId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        //  Setters
        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setCellPhone(String cellPhone) {
            this.cellPhone = cellPhone;
            return this;
        }

        public Builder setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder setAccountStatus(AccountStatus accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public Builder setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
            this.emailVerifiedAt = emailVerifiedAt;
            return this;
        }

        public Builder setCampusId(Long campusId) {
            this.campusId = campusId;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder copy(User user) {
            this.userId = user.userId;
            this.email = user.email;
            this.firstName = user.firstName;
            this.middleName = user.middleName;
            this.lastName = user.lastName;
            this.cellPhone = user.cellPhone;
            this.passwordHash = user.passwordHash;
            this.dateOfBirth = user.dateOfBirth;
            this.accountStatus = user.accountStatus;
            this.emailVerifiedAt = user.emailVerifiedAt;
            this.campusId = user.campusId;
            this.createdAt = user.createdAt;
            this.updatedAt = user.updatedAt;
            return this;
        }

        //  build method
        public User build() {
            return new User(this);
        }
    }
}