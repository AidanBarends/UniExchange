/*
 BulletinPostImageServiceImpl.java

 Business logic for BulletinPostImage. Implements the generic CRUD contract
 IService<BulletinPostImage, Long> plus the BulletinPostImage-specific
 operations.

 Author: Aidan Barends 230255639
 Date: 17 September 2026
*/

package za.ac.cput.service.community;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.community.BulletinPostImage;
import za.ac.cput.repository.community.BulletinPostImageRepository;

@Service
public class BulletinPostImageServiceImpl implements IBulletinPostImageService {

    private final BulletinPostImageRepository repository;

    public BulletinPostImageServiceImpl(BulletinPostImageRepository repository) {
        this.repository = repository;
    }

    @Override
    public BulletinPostImage create(BulletinPostImage bulletinPostImage) {
        return this.repository.save(bulletinPostImage);
    }

    @Override
    public BulletinPostImage read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public BulletinPostImage update(BulletinPostImage bulletinPostImage) {
        return this.repository.save(bulletinPostImage);
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
    public List<BulletinPostImage> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<BulletinPostImage> findByBulletinPostId(long bulletinPostId) {
        return this.repository.findByBulletinPostIdOrderByPositionAsc(bulletinPostId);
    }

    @Override
    public BulletinPostImage findPrimaryForPost(long bulletinPostId) {
        return this.repository.findByBulletinPostIdAndIsPrimaryTrue(bulletinPostId).stream()
                .findFirst().orElse(null);
    }

}
