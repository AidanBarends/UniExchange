/*
 IBulletinPostService.java

 Service contract for BulletinPost.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026

 Edited: Aidan Barends 230255639
 Date: 15 September 2026
 Added findByCategory to the service contract.
*/

package za.ac.cput.service.community;

import java.util.List;

import za.ac.cput.domain.community.BulletinPost;
import za.ac.cput.domain.enums.BulletinPostCategory;
import za.ac.cput.service.IService;

public interface IBulletinPostService extends IService<BulletinPost, Long> {

    List<BulletinPost> findByAuthorId(long authorId);

    List<BulletinPost> findAnnouncements();

    List<BulletinPost> findByCategory(BulletinPostCategory category);

}
