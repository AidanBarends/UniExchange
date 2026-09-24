/*
 ChatMediaRepository.java

 Spring Data JPA repository for the ChatMedia entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.repository.communication;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.communication.ChatMedia;

@Repository
public interface ChatMediaRepository extends JpaRepository<ChatMedia, Long> {

    /** Attachments for a page of messages - one query rather than one per bubble. */
    List<ChatMedia> findByMessageIdIn(List<Long> messageIds);

    /*
     Uploads that were never sent: the student picked a file, it uploaded, and
     then they closed the tab. Swept on a schedule, otherwise they accumulate on
     disk forever with nothing pointing at them.
    */
    List<ChatMedia> findByMessageIdIsNullAndCreatedAtBefore(LocalDateTime cutoff);

    /** Backs the per-student upload quota. */
    @Query("select coalesce(sum(m.sizeBytes), 0) from ChatMedia m " +
           "where m.uploaderId = :uploaderId and m.createdAt >= :since")
    long sumSizeBytesUploadedSince(@Param("uploaderId") long uploaderId,
                                   @Param("since") LocalDateTime since);

}
