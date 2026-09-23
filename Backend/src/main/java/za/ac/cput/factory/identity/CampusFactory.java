/*
 CampusFactory.java

 Factory for Campus. All construction goes through here so that every
 Campus is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.identity;

import za.ac.cput.domain.identity.Campus;
import za.ac.cput.util.Helper;

public class CampusFactory {

    // Prevent instantiation - factory class
    private CampusFactory() {}

    public static Campus createCampus(String name, String city, String address) {
        if (Helper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Campus: name is required");
        }

        if (Helper.isNullOrEmpty(city)) {
            throw new IllegalArgumentException("Campus: city is required");
        }

        return new Campus.Builder()
                .setName(name)
                .setCity(city)
                .setAddress(address)
                .build();
    }

    public static Campus updateCampus(Campus existing, String name, String city, String address) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Campus: existing record is required for an update");
        }

        if (Helper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Campus: name is required");
        }

        if (Helper.isNullOrEmpty(city)) {
            throw new IllegalArgumentException("Campus: city is required");
        }

        return new Campus.Builder()
                .copy(existing)
                .setName(name)
                .setCity(city)
                .setAddress(address)
                .build();
    }

}
