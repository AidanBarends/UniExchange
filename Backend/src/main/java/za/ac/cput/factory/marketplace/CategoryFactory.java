/*
 CategoryFactory.java

 Factory for Category. All construction goes through here so that every
 Category is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.marketplace;

import za.ac.cput.domain.marketplace.Category;
import za.ac.cput.util.Helper;

public class CategoryFactory {

    // Prevent instantiation - factory class
    private CategoryFactory() {}

    public static Category createCategory(String name, String description) {
        if (Helper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Category: name is required");
        }

        return new Category.Builder()
                .setName(name)
                .setDescription(description)
                .build();
    }

    public static Category updateCategory(Category existing, String name, String description) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Category: existing record is required for an update");
        }

        if (Helper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Category: name is required");
        }

        return new Category.Builder()
                .copy(existing)
                .setName(name)
                .setDescription(description)
                .build();
    }

}
