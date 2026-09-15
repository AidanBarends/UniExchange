/*
 BulletinPostRepository.java

 Spring Data JPA repository for the BulletinPost entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026

 Edited: Aidan Barends 230255639
 Date: 15 September 2026
 Added findByCategory query for filtering posts by category.
*/

package za.ac.cput.repository.community;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.community.BulletinPost;
import za.ac.cput.domain.enums.BulletinPostCategory;
import za.ac.cput.domain.enums.BulletinPostStatus;

@Repository
public interface BulletinPostRepository extends JpaRepository<BulletinPost, Long> {

    List<BulletinPost> findByAuthorId(long authorId);

    List<BulletinPost> findByStatus(BulletinPostStatus status);

    List<BulletinPost> findByIsFacultyAnnouncementTrue();

    List<BulletinPost> findByCategory(BulletinPostCategory category);

}
