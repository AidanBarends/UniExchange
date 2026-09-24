/*
 BulletinPostServiceImpl.java

 Business logic for BulletinPost. Implements the generic CRUD contract
 IService<BulletinPost, Long> plus the BulletinPost-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.community;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.community.BulletinPost;
import za.ac.cput.domain.community.BulletinPostImage;
import za.ac.cput.domain.enums.BulletinPostCategory;
import za.ac.cput.repository.community.BulletinPostRepository;
import za.ac.cput.storage.LocalFileStorage;

@Service
public class BulletinPostServiceImpl implements IBulletinPostService {

    private final BulletinPostRepository repository;
    private final IBulletinPostImageService imageService;
    private final LocalFileStorage storage;

    public BulletinPostServiceImpl(BulletinPostRepository repository, IBulletinPostImageService imageService,
                                   LocalFileStorage storage) {
        this.repository = repository;
        this.imageService = imageService;
        this.storage = storage;
    }

    @Override
    public BulletinPost create(BulletinPost bulletinPost) {
        return this.repository.save(bulletinPost);
    }

    @Override
    public BulletinPost read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public BulletinPost update(BulletinPost bulletinPost) {
        return this.repository.save(bulletinPost);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }

        for (BulletinPostImage image : this.imageService.findByBulletinPostId(id)) {
            this.imageService.delete(image.getImageId());
            this.storage.deleteIfManaged(image.getImageUrl());
        }

        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<BulletinPost> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<BulletinPost> findByAuthorId(long authorId) {
        return this.repository.findByAuthorId(authorId);
    }

    @Override
    public List<BulletinPost> findAnnouncements() {
        return this.repository.findByIsFacultyAnnouncementTrue();
    }

    @Override
    public List<BulletinPost> findByCategory(BulletinPostCategory category) {
        return this.repository.findByCategory(category);
    }

}
