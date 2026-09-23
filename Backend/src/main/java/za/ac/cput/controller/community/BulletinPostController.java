/*
 BulletinPostController.java

 REST endpoints for BulletinPost.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.community;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.community.BulletinPost;
import za.ac.cput.dto.community.BulletinPostRequest;
import za.ac.cput.factory.community.BulletinPostFactory;
import za.ac.cput.service.community.IBulletinPostService;

@RestController
@RequestMapping("/api/bulletin-posts")
public class BulletinPostController {

    private final IBulletinPostService service;

    public BulletinPostController(IBulletinPostService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BulletinPost> create(@RequestBody BulletinPostRequest request) {
        BulletinPost created = this.service.create(BulletinPostFactory.createBulletinPost(
                request.authorId(), request.title(), request.content(), request.status(),
                request.isFacultyAnnouncement()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BulletinPost> read(@PathVariable Long id) {
        BulletinPost found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BulletinPost> update(@PathVariable Long id,
                                               @RequestBody BulletinPostRequest request) {
        BulletinPost existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(BulletinPostFactory.updateBulletinPost(
                existing, request.authorId(), request.title(), request.content(), request.status(),
                request.isFacultyAnnouncement())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<BulletinPost> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/author/{authorId}")
    public List<BulletinPost> byAuthor(@PathVariable long authorId) {
        return this.service.findByAuthorId(authorId);
    }

    @GetMapping("/announcements")
    public List<BulletinPost> announcements() {
        return this.service.findAnnouncements();
    }

}
