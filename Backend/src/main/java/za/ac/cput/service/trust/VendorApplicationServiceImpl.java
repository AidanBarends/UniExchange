/*
 VendorApplicationServiceImpl.java

 Business logic for VendorApplication. Implements the generic CRUD contract
 IService<VendorApplication, Long> plus the VendorApplication-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.VendorApplicationStatus;
import za.ac.cput.domain.trust.VendorApplication;
import za.ac.cput.repository.trust.VendorApplicationRepository;
import za.ac.cput.util.Helper;

@Service
public class VendorApplicationServiceImpl implements IVendorApplicationService {

    private final VendorApplicationRepository repository;

    public VendorApplicationServiceImpl(VendorApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public VendorApplication create(VendorApplication vendorApplication) {
        return this.repository.save(vendorApplication);
    }

    @Override
    public VendorApplication read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public VendorApplication update(VendorApplication vendorApplication) {
        return this.repository.save(vendorApplication);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<VendorApplication> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<VendorApplication> findByStatus(VendorApplicationStatus status) {
        return this.repository.findByStatus(status);
    }

    @Override
    public VendorApplication decide(Long vendorApplicationId, VendorApplicationStatus decision, long reviewedBy, String reviewNote) {
        VendorApplication found = read(vendorApplicationId);
        if (found == null) {
            return null;
        }
        if (!Helper.isValidId(reviewedBy)) {
            throw new IllegalArgumentException("VendorApplication: reviewedBy must be a positive id");
        }
        return this.repository.save(new VendorApplication.Builder()
                .copy(found)
                .setStatus(decision)
                .setReviewedBy(reviewedBy)
                .setReviewNote(reviewNote)
                .setReviewedAt(LocalDateTime.now())
                .build());
    }

}
