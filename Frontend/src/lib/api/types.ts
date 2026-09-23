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

export type BulletinPost = {
  bulletinPostId: number
  authorId: number
  title: string
  content: string
  status: BulletinPostStatus
  /** Sent as `isFacultyAnnouncement`, received as `facultyAnnouncement`. */
  facultyAnnouncement: boolean
  createdAt: string
  updatedAt: string
  removedAt: string | null
}
