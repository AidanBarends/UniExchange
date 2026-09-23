/*
 IVendorApplicationService.java

 Service contract for VendorApplication.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import java.util.List;

import za.ac.cput.domain.enums.VendorApplicationStatus;
import za.ac.cput.domain.trust.VendorApplication;
import za.ac.cput.service.IService;

public interface IVendorApplicationService extends IService<VendorApplication, Long> {

    List<VendorApplication> findByStatus(VendorApplicationStatus status);

    VendorApplication decide(Long vendorApplicationId, VendorApplicationStatus decision, long reviewedBy, String reviewNote);

}
