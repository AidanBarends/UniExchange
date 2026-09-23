/*
 UserRole.java

 UserRole POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_role")
public class UserRole {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userRoleId;

    @Column(nullable = false, name = "user_id")
    private long userId;

    @Column(nullable = false, name = "role_id")
    private long roleId;

    @Column(nullable = false, name = "assigned_at")
    private LocalDateTime assignedAt;

    //  Constructors
    protected UserRole() {
        // Required by JPA
    }

    private UserRole(Builder builder) {
        this.userRoleId = builder.userRoleId;
        this.userId = builder.userId;
        this.roleId = builder.roleId;
        this.assignedAt = builder.assignedAt;
    }

    //  Getters
    public long getUserRoleId() {
        return userRoleId;
    }

    public long getUserId() {
        return userId;
    }

    public long getRoleId() {
        return roleId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "UserRole{" +
                "userRoleId=" + userRoleId +
                ", userId=" + userId +
                ", roleId=" + roleId +
                ", assignedAt=" + assignedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long userRoleId;
        private long userId;
        private long roleId;
        private LocalDateTime assignedAt;

        //  Setters
        public Builder setUserRoleId(long userRoleId) {
            this.userRoleId = userRoleId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setRoleId(long roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder setAssignedAt(LocalDateTime assignedAt) {
            this.assignedAt = assignedAt;
            return this;
        }

        public Builder copy(UserRole userRole) {
            this.userRoleId = userRole.userRoleId;
            this.userId = userRole.userId;
            this.roleId = userRole.roleId;
            this.assignedAt = userRole.assignedAt;
            return this;
        }

        //  build method
        public UserRole build() {
            return new UserRole(this);
        }
    }
}