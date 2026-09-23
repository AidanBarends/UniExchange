/*
 Role.java

 Role POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import za.ac.cput.domain.enums.RoleType;

@Entity
@Table(name = "`role`")
public class Role {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long roleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;

    @Column(length = 255)
    private String description;

    //  Constructors
    protected Role() {
        // Required by JPA
    }

    private Role(Builder builder) {
        this.roleId = builder.roleId;
        this.name = builder.name;
        this.description = builder.description;
    }

    //  Getters
    public long getRoleId() {
        return roleId;
    }

    public RoleType getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    //  toString
    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", name=" + name +
                ", description='" + description + '\'' +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long roleId;
        private RoleType name;
        private String description;

        //  Setters
        public Builder setRoleId(long roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder setName(RoleType name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder copy(Role role) {
            this.roleId = role.roleId;
            this.name = role.name;
            this.description = role.description;
            return this;
        }

        //  build method
        public Role build() {
            return new Role(this);
        }
    }
}