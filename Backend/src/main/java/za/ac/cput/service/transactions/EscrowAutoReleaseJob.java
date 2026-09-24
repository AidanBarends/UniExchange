/*
 EscrowAutoReleaseJob.java

 Releases escrows the buyer never got round to confirming.

 Without this, a buyer who receives their item and simply forgets to press
 "confirm" leaves the seller's money frozen forever, with no way out - there is no
 dispute state and no admin screen. Sellers would stop accepting wallet payments,
 which would take the whole feature with it.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.exception.ConflictException;
import za.ac.cput.repository.transactions.TransactionRepository;

@Component
public class EscrowAutoReleaseJob {

    private static final Logger log = LoggerFactory.getLogger(EscrowAutoReleaseJob.class);

    private final TransactionRepository transactionRepository;
    private final EscrowServiceImpl escrowService;
    private final int holdDays;

    public EscrowAutoReleaseJob(TransactionRepository transactionRepository,
                                EscrowServiceImpl escrowService,
                                @Value("${app.escrow.auto-release-days:7}") int holdDays) {
        this.transactionRepository = transactionRepository;
        this.escrowService = escrowService;
        this.holdDays = holdDays;
    }

    @Scheduled(cron = "0 15 * * * *")
    public void releaseExpiredHolds() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(this.holdDays);

        // Ids, not entities: loading Transactions here would put them in this
        // method's persistence context, and each release runs in its own
        // transaction where a stale copy would be a liability.
        List<Long> expired = this.transactionRepository.findPendingIdsCreatedBefore(cutoff);

        for (Long transactionId : expired) {
            try {
                Transaction transaction = this.transactionRepository.findById(transactionId).orElse(null);
                if (transaction == null) continue;

                this.escrowService.release(transaction,
                        "Released automatically after " + this.holdDays + " days");
            } catch (ConflictException raced) {
                // The buyer confirmed or cancelled between the query and now. The
                // compare-and-set inside release() is what caught it, which is
                // exactly its job - nothing to do.
                log.debug("Transaction {} was resolved before auto-release", transactionId);
            } catch (RuntimeException e) {
                // One bad transaction must not stop the rest of the batch.
                log.error("Could not auto-release transaction {}", transactionId, e);
            }
        }

        if (!expired.isEmpty()) {
            log.info("Auto-released {} escrow(s) older than {} days", expired.size(), this.holdDays);
        }
    }

}
