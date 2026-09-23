/*
 CategoryRequest.java

 Inbound payload for creating/updating a Category. Entities have no public
 setters, so requests arrive as a record and are handed to CategoryFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.marketplace;

public record CategoryRequest(
        String name,
        String description) {
}
