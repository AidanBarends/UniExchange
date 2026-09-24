/*
  Shapes returned by the backend, mirrored from the JPA entities in
  Backend/src/main/java/za/ac/cput/domain/.

  Two things to know before you use these:

  1. BOOLEAN FIELD NAMES DIFFER BETWEEN REQUEST AND RESPONSE.
     The entities declare `isPrimary` / `isRead` / `isFacultyAnnouncement`, but
     their getters are isPrimary() / isRead() / isFacultyAnnouncement(), and
     Jackson strips the "is" prefix when serialising. So you SEND
     { "isPrimary": true } to the request DTO but you READ back
     { "primary": true }. The response types below use the stripped names.
     Confirm it the first time you call one of these - if the value comes back
     undefined, that is the reason, and the fix is one word.

  2. Foreign keys are plain numbers, not nested objects. There are no JPA
     relationship annotations in the domain, so a Listing gives you `sellerId`,
     not a `seller` object. Fetch the related record separately.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

/* ------------------------------------------------------------------ identity */

export type AccountStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED'

export type RoleType = 'STUDENT' | 'FACULTY' | 'VENDOR' | 'RESIDENT' | 'ADMIN'

export type User = {
  userId: number
  email: string
  firstName: string
  middleName: string | null
  lastName: string
  cellPhone: string | null
  dateOfBirth: string | null
  accountStatus: AccountStatus
  emailVerifiedAt: string | null
  campusId: number | null
  createdAt: string
  updatedAt: string
}

export type Campus = {
  campusId: number
  name: string
  city: string
  address: string | null
}

export type AuthResponse = {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  email: string
  roles: string[]
  /*
    Proof this browser completed an OTP, so the next sign-in can skip it.
    Populated ONLY by /verify-otp - a trusted /login leaves it null, because the
    browser already holds a valid one. Store it with writeDeviceToken().
  */
  deviceToken: string | null
}

export type RegistrationResponse = {
  email: string
  message: string
  codeExpiresInSeconds: number
}

/* --------------------------------------------------------------- marketplace */

export type ListingStatus = 'ACTIVE' | 'SOLD' | 'REMOVED' | 'DELETED'

export type Listing = {
  listingId: number
  sellerId: number
  categoryId: number
  campusId: number
  title: string
  description: string | null
  /** BigDecimal on the backend; arrives as a JSON number. */
  price: number
  status: ListingStatus
  createdAt: string
  updatedAt: string
  deletedAt: string | null
}

export type Category = {
  categoryId: number
  name: string
  description: string | null
}

export type ListingImage = {
  imageId: number
  listingId: number
  imageUrl: string
  position: number
  /** Sent as `isPrimary`, received as `primary` - see the note at the top. */
  primary: boolean
}

/* ------------------------------------------------------------- communication */

export type NotificationType = 'MESSAGE' | 'LISTING' | 'TRANSACTION' | 'BULLETIN' | 'SYSTEM'

export type Notification = {
  notificationId: number
  userId: number
  type: NotificationType
  title: string
  content: string | null
  entityType: string | null
  entityId: number | null
  /** Sent as `isRead`, received as `read` - see the note at the top. */
  read: boolean
  createdAt: string
}

export type Conversation = {
  conversationId: number
  createdAt: string
}

export type ConversationParticipant = {
  participantId: number
  conversationId: number
  userId: number
  joinedAt: string
  lastReadAt: string | null
}

export type Message = {
  messageId: number
  conversationId: number
  senderId: number
  content: string
  sentAt: string
}

/* ----------------------------------------------------------------- community */

export type BulletinPostStatus = 'PUBLISHED' | 'HIDDEN' | 'REMOVED'

export type BulletinPostCategory = 'GENERAL' | 'EVENT' | 'STUDY_GROUP' | 'LOST_AND_FOUND'

export type BulletinPost = {
  bulletinPostId: number
  authorId: number
  title: string
  content: string
  status: BulletinPostStatus
  /** Sent as `isFacultyAnnouncement`, received as `facultyAnnouncement`. */
  facultyAnnouncement: boolean
  category: BulletinPostCategory
  createdAt: string
  updatedAt: string
  removedAt: string | null
}

export type Review = {
  reviewId: number
  transactionId: number
  reviewerId: number
  revieweeId: number
  /** 1–5 */
  rating: number
  comment: string | null
  createdAt: string
}
 
export type TrustedSellerBadge = {
  trustedSellerBadgeId: number
  userId: number
  earnedAt: string
  /** null while the badge is still active */
  revokedAt: string | null
}
export type BulletinPostImage = {
  imageId: number
  bulletinPostId: number
  imageUrl: string
  position: number
  /** Sent as `isPrimary`, received as `primary` - see the note at the top. */
  primary: boolean
}

/* -------------------------------------------------------------------- chat */

export type ChatMediaType = 'IMAGE' | 'VIDEO' | 'AUDIO'

export type ChatMediaView = {
  mediaId: number
  mediaType: ChatMediaType
  mimeType: string
  /**
   * Pre-signed, short-lived and bound to you as the viewer. Drop it straight
   * into an img/audio/video src - do NOT prefix BASE_URL, and do not cache it in
   * component state across polls, because it rotates roughly every half hour.
   */
  url: string
  /**
   * Milliseconds, for voice notes and video. Measured in the browser while
   * recording, because a MediaRecorder blob reports Infinity for its duration -
   * so render your own progress bar from this rather than trusting the element.
   */
  durationMs: number | null
  sizeBytes: number
  originalFilename: string | null
}

export type ChatMessageView = {
  messageId: number
  conversationId: number
  senderId: number
  /** Empty string for an attachment sent with no caption. */
  content: string
  sentAt: string
  media: ChatMediaView | null
}

export type ChatParticipant = {
  userId: number
  firstName: string
  lastName: string
}

export type ChatThreadView = {
  conversationId: number
  /** Null for a general chat, and for every conversation started before listings were linked. */
  listingId: number | null
  listingTitle: string | null
  otherParticipant: ChatParticipant | null
  lastMessagePreview: string | null
  lastMessageAt: string | null
  unreadCount: number
}

export type ChatMediaUploaded = {
  mediaId: number
  mediaType: ChatMediaType
  durationMs: number | null
}

/* ------------------------------------------------------------ transactions */

export type TransactionStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'FAILED'

export type PaymentMethod = 'CASH' | 'WALLET' | 'PAYFAST' | 'SNAPSCAN'

export type WalletTransactionType = 'CREDIT' | 'DEBIT' | 'REFUND' | 'ADJUSTMENT'

/**
 * Money arrives as a JSON number, not a string: Jackson serialises the
 * backend's BigDecimal that way, and the existing Listing.price does the same.
 *
 * Safe to display and compare at marketplace amounts, but do NOT do arithmetic
 * on it and send the result back - all money maths belongs on the server, where
 * it stays in BigDecimal.
 */
export type WalletSummary = {
  /**
   * Spendable right now. This is ALREADY net of anything in escrow - money
   * leaves the wallet the moment a purchase is made - so never subtract `held`
   * from it, or you deduct the same amount twice.
   */
  available: number
  /** Paid for, not yet released to the seller. */
  held: number
  /** available + held. */
  total: number
  currency: string
  /**
   * What a top-up will actually do in this environment, so the UI can say so
   * up front instead of describing a payment screen the student may never see.
   *
   *   LIVE      - real PayFast, real money
   *   SANDBOX   - PayFast's test environment: a real payment screen, no money
   *   SIMULATED - local development: completed in-app, PayFast never contacted
   */
  topUpMode: 'LIVE' | 'SANDBOX' | 'SIMULATED'
}

/** Result of a transfer. `balanceAfter` is the SENDER's new balance. */
export type TransferResult = {
  amount: number
  recipientName: string
  balanceAfter: number
}

export type WalletTransaction = {
  walletTransactionId: number
  walletId: number
  type: WalletTransactionType
  amount: number
  balanceAfter: number
  referenceType: string | null
  referenceId: number | null
  description: string | null
  createdAt: string
}

export type Transaction = {
  transactionId: number
  buyerId: number
  sellerId: number
  listingId: number
  amount: number
  paymentMethod: PaymentMethod
  status: TransactionStatus
  createdAt: string
  completedAt: string | null
}

/**
 * The fields to POST to PayFast, in order.
 *
 * Render these as a hidden self-submitting form rather than building a URL: the
 * signature covers the values, so anything altered in transit is rejected.
 */
export type PayFastRedirect = {
  processUrl: string
  fields: Record<string, string>
  /** Our reference for this attempt, used to ask about it afterwards. */
  merchantPaymentId: string
  /**
   * True only in local development with the ITN simulator switched on.
   *
   * PayFast confirms a payment by calling the backend from their own servers,
   * which can never reach localhost - so on a laptop the redirect is a dead end.
   * When this is true the UI completes the top-up itself instead of redirecting.
   */
  simulatorEnabled: boolean
}
