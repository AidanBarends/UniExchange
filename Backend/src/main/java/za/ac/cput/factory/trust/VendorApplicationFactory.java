/*
 VendorApplicationFactory.java

 Factory for VendorApplication. All construction goes through here so that every
 VendorApplication is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.trust;

import java.time.LocalDateTime;

import za.ac.cput.domain.enums.VendorApplicationStatus;
import za.ac.cput.domain.trust.VendorApplication;
import za.ac.cput.util.Helper;

public class VendorApplicationFactory {

    // Prevent instantiation - factory class
    private VendorApplicationFactory() {}

    public static VendorApplication createVendorApplication(long applicantId, String businessName,
                                                            String businessDescription,
                                                            VendorApplicationStatus status) {
        if (!Helper.isValidId(applicantId)) {
            throw new IllegalArgumentException("VendorApplication: applicantId must be a positive id");
        }

        if (Helper.isNullOrEmpty(businessName)) {
            throw new IllegalArgumentException("VendorApplication: businessName is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("VendorApplication: status is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new VendorApplication.Builder()
                .setApplicantId(applicantId)
                .setBusinessName(businessName)
                .setBusinessDescription(businessDescription)
                .setStatus(status)
                .setCreatedAt(now)
                .build();
    }

    public static VendorApplication updateVendorApplication(VendorApplication existing, long applicantId,
                                                            String businessName, String businessDescription,
                                                            VendorApplicationStatus status) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("VendorApplication: existing record is required for an update");
        }

        if (!Helper.isValidId(applicantId)) {
            throw new IllegalArgumentException("VendorApplication: applicantId must be a positive id");
        }

        if (Helper.isNullOrEmpty(businessName)) {
            throw new IllegalArgumentException("VendorApplication: businessName is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("VendorApplication: status is required");
        }

        return new VendorApplication.Builder()
                .copy(existing)
                .setApplicantId(applicantId)
                .setBusinessName(businessName)
                .setBusinessDescription(businessDescription)
                .setStatus(status)
                .build();
    }

}
