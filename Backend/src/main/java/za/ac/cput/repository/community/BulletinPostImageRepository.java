/*
 BulletinPostImageRepository.java

 Spring Data JPA repository for the BulletinPostImage entity.

 Author: Aidan Barends 230255639
 Date: 17 September 2026
*/

package za.ac.cput.repository.community;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.community.BulletinPostImage;

@Repository
public interface BulletinPostImageRepository extends JpaRepository<BulletinPostImage, Long> {

    List<BulletinPostImage> findByBulletinPostIdOrderByPositionAsc(long bulletinPostId);

    List<BulletinPostImage> findByBulletinPostIdAndIsPrimaryTrue(long bulletinPostId);

}
