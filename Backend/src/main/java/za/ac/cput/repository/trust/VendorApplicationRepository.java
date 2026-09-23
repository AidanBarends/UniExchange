/*
 VendorApplicationRepository.java

 Spring Data JPA repository for the VendorApplication entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.trust;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.VendorApplicationStatus;
import za.ac.cput.domain.trust.VendorApplication;

@Repository
public interface VendorApplicationRepository extends JpaRepository<VendorApplication, Long> {

    List<VendorApplication> findByApplicantId(long applicantId);

    List<VendorApplication> findByStatus(VendorApplicationStatus status);

}
