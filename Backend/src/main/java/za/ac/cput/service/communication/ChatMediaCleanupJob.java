/*
 ChatMediaCleanupJob.java

 Removes chat attachments that were uploaded but never sent.

 The upload and the message are two separate requests, so a student who picks a
 photo and then closes the tab leaves a file on disk with no message pointing at
 it. Nothing else will ever reference it, and on Azure App Service the /home
 share is small enough (1GB on Free, 10GB on Basic, shared with the deployed
 app) that this matters within a term rather than eventually.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.communication;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.communication.ChatMedia;
import za.ac.cput.repository.communication.ChatMediaRepository;
import za.ac.cput.storage.ChatMediaStorage;

@Component
public class ChatMediaCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ChatMediaCleanupJob.class);

    /*
     Generous on purpose. The only cost of waiting is disk, while deleting too
     eagerly would pull the file out from under someone who uploaded a video on a
     slow campus connection and is still typing their caption.
    */
    private static final int ORPHAN_AGE_HOURS = 24;

    private final ChatMediaRepository repository;
    private final ChatMediaStorage storage;

    public ChatMediaCleanupJob(ChatMediaRepository repository, ChatMediaStorage storage) {
        this.repository = repository;
        this.storage = storage;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void sweepUnattachedMedia() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(ORPHAN_AGE_HOURS);
        List<ChatMedia> stale = this.repository.findByMessageIdIsNullAndCreatedAtBefore(cutoff);

        for (ChatMedia media : stale) {
            /*
             Row first, file second. If the delete is rolled back after the file is
             already gone, the row survives pointing at nothing and the student gets
             a broken attachment. The other way round leaves a stray file, which is
             only wasted disk. Same reasoning as LocalFileStorage.deleteIfManaged.

             DELETE is idempotent, so two App Service instances both running this
             job is harmless.
            */
            this.repository.deleteById(media.getChatMediaId());
            this.storage.deleteQuietly(media.getStorageKey());
        }

        if (!stale.isEmpty()) {
            log.info("Removed {} unsent chat attachment(s) older than {}h", stale.size(), ORPHAN_AGE_HOURS);
        }
    }

}
