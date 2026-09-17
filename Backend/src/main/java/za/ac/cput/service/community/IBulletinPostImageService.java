/*
 IBulletinPostImageService.java

 Service contract for BulletinPostImage.

 Author: Aidan Barends 230255639
 Date: 17 September 2026
*/

package za.ac.cput.service.community;

import java.util.List;

import za.ac.cput.domain.community.BulletinPostImage;
import za.ac.cput.service.IService;

public interface IBulletinPostImageService extends IService<BulletinPostImage, Long> {

    List<BulletinPostImage> findByBulletinPostId(long bulletinPostId);

    BulletinPostImage findPrimaryForPost(long bulletinPostId);

}
