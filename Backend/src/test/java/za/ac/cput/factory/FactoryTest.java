/*
 FactoryTest.java

 JUnit 5 replacement for the "build one of each entity" section of the old
 DomainTest main-method runner, which used to live (incorrectly) in src/main/java.

 Builds one instance of all 22 entities through its factory, checks the values
 survive the Builder round-trip, and checks that each factory rejects bad input
 with IllegalArgumentException rather than persisting nonsense.

 Pure unit test - no Spring context, no database.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.ac.cput.domain.admin.AuditLog;
import za.ac.cput.domain.communication.Conversation;
import za.ac.cput.domain.communication.ConversationParticipant;
import za.ac.cput.domain.communication.Message;
import za.ac.cput.domain.communication.Notification;
import za.ac.cput.domain.community.BulletinPost;
import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.enums.BulletinPostStatus;
import za.ac.cput.domain.enums.ListingStatus;
import za.ac.cput.domain.enums.NotificationType;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.enums.ReportStatus;
import za.ac.cput.domain.enums.ReportTargetType;
import za.ac.cput.domain.enums.RoleType;
import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.enums.VendorApplicationStatus;
import za.ac.cput.domain.enums.VerificationType;
import za.ac.cput.domain.enums.WalletTransactionType;
import za.ac.cput.domain.identity.Campus;
import za.ac.cput.domain.identity.Role;
import za.ac.cput.domain.identity.TrustedDevice;
import za.ac.cput.domain.identity.User;
import za.ac.cput.domain.identity.UserRole;
import za.ac.cput.domain.identity.Verification;
import za.ac.cput.domain.marketplace.Category;
import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.domain.marketplace.ListingImage;
import za.ac.cput.domain.transactions.Payment;
import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.domain.trust.Report;
import za.ac.cput.domain.trust.Review;
import za.ac.cput.domain.trust.TrustedSellerBadge;
import za.ac.cput.domain.trust.VendorApplication;
import za.ac.cput.factory.admin.AuditLogFactory;
import za.ac.cput.factory.communication.ConversationFactory;
import za.ac.cput.factory.communication.ConversationParticipantFactory;
import za.ac.cput.factory.communication.MessageFactory;
import za.ac.cput.factory.communication.NotificationFactory;
import za.ac.cput.factory.community.BulletinPostFactory;
import za.ac.cput.factory.identity.CampusFactory;
import za.ac.cput.factory.identity.RoleFactory;
import za.ac.cput.factory.identity.TrustedDeviceFactory;
import za.ac.cput.factory.identity.UserFactory;
import za.ac.cput.factory.identity.UserRoleFactory;
import za.ac.cput.factory.identity.VerificationFactory;
import za.ac.cput.factory.marketplace.CategoryFactory;
import za.ac.cput.factory.marketplace.ListingFactory;
import za.ac.cput.factory.marketplace.ListingImageFactory;
import za.ac.cput.factory.transactions.PaymentFactory;
import za.ac.cput.factory.transactions.TransactionFactory;
import za.ac.cput.factory.transactions.WalletFactory;
import za.ac.cput.factory.transactions.WalletTransactionFactory;
import za.ac.cput.factory.trust.ReportFactory;
import za.ac.cput.factory.trust.ReviewFactory;
import za.ac.cput.factory.trust.TrustedSellerBadgeFactory;
import za.ac.cput.factory.trust.VendorApplicationFactory;

class FactoryTest {

    private static final BigDecimal PRICE = new BigDecimal("199.99");

    @Nested
    class Identity {

        @Test
        void campus() {
            Campus campus = CampusFactory.createCampus("District Six", "Cape Town", "Hanover Street");

            assertEquals("District Six", campus.getName());
            assertEquals("Cape Town", campus.getCity());
            assertEquals("Hanover Street", campus.getAddress());
        }

        @Test
        void campusRejectsBlankName() {
            assertThrows(IllegalArgumentException.class,
                    () -> CampusFactory.createCampus("  ", "Cape Town", "Hanover Street"));
        }

        @Test
        void role() {
            Role role = RoleFactory.createRole(RoleType.STUDENT, "Registered student");

            assertEquals(RoleType.STUDENT, role.getName());
            assertEquals("Registered student", role.getDescription());
        }

        @Test
        void roleRejectsNullName() {
            assertThrows(IllegalArgumentException.class,
                    () -> RoleFactory.createRole(null, "Registered student"));
        }

        @Test
        void user() {
            User user = UserFactory.createUser("student@mycput.ac.za", "Yaseen", null,
                    "Kannemeyer", "0821234567", "hashed-password",
                    LocalDate.of(2003, 5, 17), AccountStatus.PENDING_VERIFICATION, 1L);

            assertEquals("student@mycput.ac.za", user.getEmail());
            assertEquals("Yaseen", user.getFirstName());
            assertEquals("Kannemeyer", user.getLastName());
            assertEquals(AccountStatus.PENDING_VERIFICATION, user.getAccountStatus());
            assertEquals(1L, user.getCampusId());
            // Timestamps are set by the factory, not by a MySQL column default,
            // so the same code works against H2 and against MySQL.
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
        }

        @Test
        void userRejectsBadEmail() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserFactory.createUser("not-an-email", "Yaseen", null, "Kannemeyer",
                            "0821234567", "hashed-password", null, AccountStatus.ACTIVE, null));
        }

        @Test
        void userRejectsBadCellPhone() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserFactory.createUser("student@mycput.ac.za", "Yaseen", null, "Kannemeyer",
                            "082-123", "hashed-password", null, AccountStatus.ACTIVE, null));
        }

        @Test
        void userAllowsOmittedCellPhone() {
            User user = UserFactory.createUser("student@mycput.ac.za", "Yaseen", null, "Kannemeyer",
                    null, "hashed-password", null, AccountStatus.ACTIVE, null);

            assertNull(user.getCellPhone());
        }

        @Test
        void userUpdatePreservesCreatedAt() {
            User user = UserFactory.createUser("student@mycput.ac.za", "Yaseen", null, "Kannemeyer",
                    null, "hashed-password", null, AccountStatus.PENDING_VERIFICATION, null);

            User activated = UserFactory.updateUser(user, user.getEmail(), user.getFirstName(),
                    user.getMiddleName(), user.getLastName(), user.getCellPhone(),
                    user.getPasswordHash(), user.getDateOfBirth(), AccountStatus.ACTIVE,
                    user.getCampusId());

            assertEquals(AccountStatus.ACTIVE, activated.getAccountStatus());
            assertEquals(user.getCreatedAt(), activated.getCreatedAt());
            assertEquals(user.getEmail(), activated.getEmail());
        }

        @Test
        void userRole() {
            UserRole userRole = UserRoleFactory.createUserRole(1L, 2L);

            assertEquals(1L, userRole.getUserId());
            assertEquals(2L, userRole.getRoleId());
            assertNotNull(userRole.getAssignedAt());
        }

        @Test
        void userRoleRejectsZeroId() {
            assertThrows(IllegalArgumentException.class, () -> UserRoleFactory.createUserRole(0L, 2L));
        }

        @Test
        void verification() {
            LocalDateTime expiry = LocalDateTime.now().plusHours(24);
            Verification verification = VerificationFactory.createVerification(
                    1L, VerificationType.EMAIL, "token-abc", expiry);

            assertEquals(VerificationType.EMAIL, verification.getVerificationType());
            assertEquals("token-abc", verification.getToken());
            assertEquals(expiry, verification.getExpiresAt());
            assertNull(verification.getVerifiedAt());
        }

        @Test
        void verificationRejectsMissingExpiry() {
            assertThrows(IllegalArgumentException.class, () -> VerificationFactory.createVerification(
                    1L, VerificationType.EMAIL, "token-abc", null));
        }

        @Test
        void trustedDevice() {
            LocalDateTime expiry = LocalDateTime.now().plusDays(30);
            TrustedDevice device = TrustedDeviceFactory.createTrustedDevice(
                    1L, "a".repeat(64), "Mozilla/5.0", true, expiry);

            assertEquals(1L, device.getUserId());
            assertEquals("a".repeat(64), device.getTokenHash());
            assertEquals("Mozilla/5.0", device.getLabel());
            assertTrue(device.isPersistent());
            assertEquals(expiry, device.getExpiresAt());
            assertNull(device.getRevokedAt());
            assertNotNull(device.getCreatedAt());
        }

        @Test
        void trustedDeviceRejectsMissingTokenHash() {
            assertThrows(IllegalArgumentException.class, () -> TrustedDeviceFactory.createTrustedDevice(
                    1L, "  ", "Mozilla/5.0", true, LocalDateTime.now().plusDays(30)));
        }

        @Test
        void trustedDeviceRejectsMissingExpiry() {
            assertThrows(IllegalArgumentException.class, () -> TrustedDeviceFactory.createTrustedDevice(
                    1L, "a".repeat(64), "Mozilla/5.0", true, null));
        }

        @Test
        void trustedDeviceRevokeKeepsEverythingElse() {
            TrustedDevice device = TrustedDeviceFactory.createTrustedDevice(
                    1L, "a".repeat(64), "Mozilla/5.0", true, LocalDateTime.now().plusDays(30));

            TrustedDevice revoked = TrustedDeviceFactory.revoke(device);

            assertNotNull(revoked.getRevokedAt());
            assertEquals(device.getTokenHash(), revoked.getTokenHash());
            assertEquals(device.getCreatedAt(), revoked.getCreatedAt());
        }

    }

    @Nested
    class Marketplace {

        @Test
        void category() {
            Category category = CategoryFactory.createCategory("Textbooks", "Prescribed books");

            assertEquals("Textbooks", category.getName());
        }

        @Test
        void listing() {
            Listing listing = ListingFactory.createListing(1L, 2L, 3L, "Calculus textbook",
                    "8th edition, good condition", PRICE, ListingStatus.ACTIVE);

            assertEquals(1L, listing.getSellerId());
            assertEquals("Calculus textbook", listing.getTitle());
            assertEquals(PRICE, listing.getPrice());
            assertEquals(ListingStatus.ACTIVE, listing.getStatus());
            assertNotNull(listing.getCreatedAt());
        }

        @Test
        void listingAllowsFreeItem() {
            Listing listing = ListingFactory.createListing(1L, 2L, 3L, "Free notes", null,
                    BigDecimal.ZERO, ListingStatus.ACTIVE);

            assertEquals(BigDecimal.ZERO, listing.getPrice());
        }

        @Test
        void listingRejectsNegativePrice() {
            assertThrows(IllegalArgumentException.class,
                    () -> ListingFactory.createListing(1L, 2L, 3L, "Calculus textbook", null,
                            new BigDecimal("-1.00"), ListingStatus.ACTIVE));
        }

        @Test
        void listingRejectsUnknownSeller() {
            assertThrows(IllegalArgumentException.class,
                    () -> ListingFactory.createListing(0L, 2L, 3L, "Calculus textbook", null,
                            PRICE, ListingStatus.ACTIVE));
        }

        @Test
        void listingImage() {
            ListingImage image = ListingImageFactory.createListingImage(
                    1L, "https://uniexchange.co.za/img/1.png", 0, true);

            assertEquals("https://uniexchange.co.za/img/1.png", image.getImageUrl());
            assertEquals(0, image.getPosition());
            assertTrue(image.isPrimary());
        }

        @Test
        void listingImageRejectsBadUrl() {
            assertThrows(IllegalArgumentException.class,
                    () -> ListingImageFactory.createListingImage(1L, "not a url", 0, true));
        }

        @Test
        void listingImageRejectsNegativePosition() {
            assertThrows(IllegalArgumentException.class, () -> ListingImageFactory.createListingImage(
                    1L, "https://uniexchange.co.za/img/1.png", -1, false));
        }

    }

    @Nested
    class Transactions {

        @Test
        void transaction() {
            Transaction transaction = TransactionFactory.createTransaction(1L, 2L, 3L, PRICE,
                    PaymentMethod.WALLET, TransactionStatus.PENDING);

            assertEquals(1L, transaction.getBuyerId());
            assertEquals(PaymentMethod.WALLET, transaction.getPaymentMethod());
            assertEquals(TransactionStatus.PENDING, transaction.getStatus());
            assertNull(transaction.getCompletedAt());
        }

        @Test
        void payment() {
            Payment payment = PaymentFactory.createPayment(1L, PRICE, PaymentMethod.PAYFAST,
                    PaymentStatus.PENDING, "PF-12345");

            assertEquals("PF-12345", payment.getExternalReference());
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
        }

        @Test
        void wallet() {
            Wallet wallet = WalletFactory.createWallet(1L, new BigDecimal("500.00"), "ZAR");

            assertEquals("ZAR", wallet.getCurrency());
            assertEquals(new BigDecimal("500.00"), wallet.getBalance());
        }

        @Test
        void walletRejectsBadCurrency() {
            assertThrows(IllegalArgumentException.class,
                    () -> WalletFactory.createWallet(1L, BigDecimal.ZERO, "rands"));
        }

        @Test
        void walletTransaction() {
            WalletTransaction movement = WalletTransactionFactory.createWalletTransaction(
                    1L, WalletTransactionType.CREDIT, new BigDecimal("100.00"),
                    new BigDecimal("600.00"), "TRANSACTION", 9L, "Sale payout");

            assertEquals(WalletTransactionType.CREDIT, movement.getType());
            assertEquals(new BigDecimal("600.00"), movement.getBalanceAfter());
            assertEquals(9L, movement.getReferenceId());
        }

        @Test
        void walletTransactionAllowsNoReference() {
            WalletTransaction movement = WalletTransactionFactory.createWalletTransaction(
                    1L, WalletTransactionType.ADJUSTMENT, BigDecimal.ONE, BigDecimal.ONE,
                    null, null, null);

            assertNull(movement.getReferenceId());
        }

    }

    @Nested
    class Trust {

        @Test
        void review() {
            Review review = ReviewFactory.createReview(1L, 2L, 3L, 5, "Great seller");

            assertEquals(5, review.getRating());
            assertEquals("Great seller", review.getComment());
        }

        @Test
        void reviewRejectsRatingOutOfRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> ReviewFactory.createReview(1L, 2L, 3L, 6, "Great seller"));
            assertThrows(IllegalArgumentException.class,
                    () -> ReviewFactory.createReview(1L, 2L, 3L, 0, "Great seller"));
        }

        @Test
        void report() {
            Report report = ReportFactory.createReport(1L, ReportTargetType.LISTING, 7L,
                    "Item is counterfeit", ReportStatus.PENDING);

            assertEquals(ReportTargetType.LISTING, report.getTargetType());
            assertEquals(7L, report.getTargetId());
            assertNull(report.getHandledBy());
        }

        @Test
        void reportRejectsBlankReason() {
            assertThrows(IllegalArgumentException.class, () -> ReportFactory.createReport(
                    1L, ReportTargetType.LISTING, 7L, "", ReportStatus.PENDING));
        }

        @Test
        void vendorApplication() {
            VendorApplication application = VendorApplicationFactory.createVendorApplication(
                    1L, "Campus Coffee", "Coffee cart at District Six",
                    VendorApplicationStatus.PENDING);

            assertEquals("Campus Coffee", application.getBusinessName());
            assertEquals(VendorApplicationStatus.PENDING, application.getStatus());
            assertNull(application.getReviewedBy());
        }

        @Test
        void trustedSellerBadge() {
            TrustedSellerBadge badge = TrustedSellerBadgeFactory.createTrustedSellerBadge(1L);

            assertEquals(1L, badge.getUserId());
            assertNotNull(badge.getEarnedAt());
            assertNull(badge.getRevokedAt());
        }

    }

    @Nested
    class Communication {

        @Test
        void conversation() {
            Conversation conversation = ConversationFactory.createConversation();

            assertNotNull(conversation.getCreatedAt());
        }

        @Test
        void conversationParticipant() {
            ConversationParticipant participant =
                    ConversationParticipantFactory.createConversationParticipant(1L, 2L);

            assertEquals(1L, participant.getConversationId());
            assertEquals(2L, participant.getUserId());
            assertNotNull(participant.getJoinedAt());
        }

        @Test
        void message() {
            Message message = MessageFactory.createMessage(1L, 2L, "Is this still available?");

            assertEquals("Is this still available?", message.getContent());
            assertNotNull(message.getSentAt());
        }

        @Test
        void messageRejectsEmptyContent() {
            assertThrows(IllegalArgumentException.class, () -> MessageFactory.createMessage(1L, 2L, " "));
        }

        @Test
        void notificationStartsUnread() {
            Notification notification = NotificationFactory.createNotification(1L,
                    NotificationType.MESSAGE, "New message", "You have a new message", "MESSAGE", 5L);

            assertEquals(NotificationType.MESSAGE, notification.getType());
            assertFalse(notification.isRead());
            assertEquals(5L, notification.getEntityId());
        }

    }

    @Nested
    class CommunityAndAdmin {

        @Test
        void bulletinPost() {
            BulletinPost post = BulletinPostFactory.createBulletinPost(1L, "Res meeting",
                    "Tuesday 18:00 in the common room", BulletinPostStatus.PUBLISHED, false);

            assertEquals("Res meeting", post.getTitle());
            assertEquals(BulletinPostStatus.PUBLISHED, post.getStatus());
            assertFalse(post.isFacultyAnnouncement());
        }

        @Test
        void bulletinPostRejectsBlankContent() {
            assertThrows(IllegalArgumentException.class, () -> BulletinPostFactory.createBulletinPost(
                    1L, "Res meeting", "", BulletinPostStatus.PUBLISHED, false));
        }

        @Test
        void auditLog() {
            AuditLog log = AuditLogFactory.createAuditLog(1L, "SUSPEND_USER", "USER", 42L,
                    "Repeated policy violations");

            assertEquals("SUSPEND_USER", log.getAction());
            assertEquals("USER", log.getTargetType());
            assertEquals(42L, log.getTargetId());
            assertNotNull(log.getCreatedAt());
        }

        @Test
        void auditLogRejectsBlankAction() {
            assertThrows(IllegalArgumentException.class,
                    () -> AuditLogFactory.createAuditLog(1L, "", "USER", 42L, null));
        }

    }

}
