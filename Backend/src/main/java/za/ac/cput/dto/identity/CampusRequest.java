/*
 CampusRequest.java

 Inbound payload for creating/updating a Campus. Entities have no public
 setters, so requests arrive as a record and are handed to CampusFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.identity;

public record CampusRequest(
        String name,
        String city,
        String address) {
}
