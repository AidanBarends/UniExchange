/*
 VendorApplicationRequest.java

 Inbound payload for creating/updating a VendorApplication. Entities have no public
 setters, so requests arrive as a record and are handed to VendorApplicationFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.trust;

import za.ac.cput.domain.enums.VendorApplicationStatus;

public record VendorApplicationRequest(
        long applicantId,
        String businessName,
        String businessDescription,
        VendorApplicationStatus status) {
}
