/*
 TrustedSellerBadgeServiceImpl.java

 Business logic for TrustedSellerBadge. Implements the generic CRUD contract
 IService<TrustedSellerBadge, Long> plus the TrustedSellerBadge-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.trust.TrustedSellerBadge;
import za.ac.cput.repository.trust.TrustedSellerBadgeRepository;

@Service
public class TrustedSellerBadgeServiceImpl implements ITrustedSellerBadgeService {

    private final TrustedSellerBadgeRepository repository;

    public TrustedSellerBadgeServiceImpl(TrustedSellerBadgeRepository repository) {
        this.repository = repository;
    }

    @Override
    public TrustedSellerBadge create(TrustedSellerBadge trustedSellerBadge) {
        return this.repository.save(trustedSellerBadge);
    }

    @Override
    public TrustedSellerBadge read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public TrustedSellerBadge update(TrustedSellerBadge trustedSellerBadge) {
        return this.repository.save(trustedSellerBadge);
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
    public List<TrustedSellerBadge> getAll() {
        return this.repository.findAll();
    }

    @Override
    public TrustedSellerBadge findByUserId(long userId) {
        return this.repository.findByUserId(userId).orElse(null);
    }

    @Override
    public TrustedSellerBadge revoke(Long trustedSellerBadgeId) {
        TrustedSellerBadge found = read(trustedSellerBadgeId);
        if (found == null) {
            return null;
        }
        return this.repository.save(new TrustedSellerBadge.Builder()
                .copy(found)
                .setRevokedAt(LocalDateTime.now())
                .build());
    }

}
