/*
 BulletinPostImageController.java

 REST endpoints for BulletinPostImage.

 Unlike ListingImageController (which has no ownership check), create/update/
 delete here confirm that the caller owns the PARENT bulletin post before
 attaching/changing/removing an image on it, mirroring the ownership check in
 BulletinPostController.

 Author: Aidan Barends 230255639
 Date: 17 September 2026
*/

package za.ac.cput.controller.community;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.community.BulletinPost;
import za.ac.cput.domain.community.BulletinPostImage;
import za.ac.cput.dto.community.BulletinPostImageRequest;
import za.ac.cput.factory.community.BulletinPostImageFactory;
import za.ac.cput.security.UniExchangeUserDetailsService.AuthenticatedUser;
import za.ac.cput.service.community.IBulletinPostImageService;
import za.ac.cput.service.community.IBulletinPostService;

@RestController
@RequestMapping("/api/bulletin-post-images")
public class BulletinPostImageController {

    private final IBulletinPostImageService service;
    private final IBulletinPostService postService;

    public BulletinPostImageController(IBulletinPostImageService service, IBulletinPostService postService) {
        this.service = service;
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<BulletinPostImage> create(@RequestBody BulletinPostImageRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser principal) {
        BulletinPost post = this.postService.read(request.bulletinPostId());
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        if (post.getAuthorId() != principal.getUser().getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        BulletinPostImage created = this.service.create(BulletinPostImageFactory.createBulletinPostImage(
                request.bulletinPostId(), request.imageUrl(), request.position(), request.isPrimary()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BulletinPostImage> read(@PathVariable Long id) {
        BulletinPostImage found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BulletinPostImage> update(@PathVariable Long id,
                                                     @RequestBody BulletinPostImageRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser principal) {
        BulletinPostImage existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isOwner(existing.getBulletinPostId(), principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(this.service.update(BulletinPostImageFactory.updateBulletinPostImage(
                existing, request.bulletinPostId(), request.imageUrl(), request.position(),
                request.isPrimary())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        BulletinPostImage existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isOwner(existing.getBulletinPostId(), principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<BulletinPostImage> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/bulletin-post/{bulletinPostId}")
    public List<BulletinPostImage> byPost(@PathVariable long bulletinPostId) {
        return this.service.findByBulletinPostId(bulletinPostId);
    }

    private boolean isOwner(long bulletinPostId, AuthenticatedUser principal) {
        BulletinPost post = this.postService.read(bulletinPostId);
        return post != null && post.getAuthorId() == principal.getUser().getUserId();
    }

}
