/*
 Campus.java

 Campus POJO class

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

@Entity
@Table(name = "campus")
public class Campus {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long campusId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 255)
    private String address;

    //  Constructors
    protected Campus() {
        // Required by JPA
    }

    private Campus(Builder builder) {
        this.campusId = builder.campusId;
        this.name = builder.name;
        this.city = builder.city;
        this.address = builder.address;
    }

    //  Getters
    public long getCampusId() {
        return campusId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    //  toString
    @Override
    public String toString() {
        return "Campus{" +
                "campusId=" + campusId +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long campusId;
        private String name;
        private String city;
        private String address;

        //  Setters
        public Builder setCampusId(long campusId) {
            this.campusId = campusId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCity(String city) {
            this.city = city;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder copy(Campus campus) {
            this.campusId = campus.campusId;
            this.name = campus.name;
            this.city = campus.city;
            this.address = campus.address;
            return this;
        }

        //  build method
        public Campus build() {
            return new Campus(this);
        }
    }
}