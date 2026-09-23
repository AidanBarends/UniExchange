/*
 VerificationServiceImpl.java

 Business logic for Verification. Implements the generic CRUD contract
 IService<Verification, Long> plus the Verification-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.identity.Verification;
import za.ac.cput.repository.identity.VerificationRepository;

@Service
public class VerificationServiceImpl implements IVerificationService {

    private final VerificationRepository repository;

    public VerificationServiceImpl(VerificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Verification create(Verification verification) {
        return this.repository.save(verification);
    }

    @Override
    public Verification read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Verification update(Verification verification) {
        return this.repository.save(verification);
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
    public List<Verification> getAll() {
        return this.repository.findAll();
    }

    @Override
    public Verification findByToken(String token) {
        return this.repository.findByToken(token).orElse(null);
    }

    @Override
    public Verification consumeToken(String token) {
        Verification found = this.repository.findByToken(token).orElse(null);
        if (found == null || found.getVerifiedAt() != null) {
            return null;
        }
        if (found.getExpiresAt() != null && found.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return this.repository.save(new Verification.Builder()
                .copy(found)
                .setVerifiedAt(LocalDateTime.now())
                .build());
    }

}
