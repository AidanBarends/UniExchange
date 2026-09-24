/*
 ChatServiceImpl.java

 The chat feature's business logic: thread lists, the polling read, sending,
 attachments, and read receipts.

 This service exists because the domain has no JPA relationships. A conversation
 row knows nothing about its participants, its messages or the listing it is
 about, so assembling a usable inbox means several queries that the frontend
 would otherwise have to fan out itself - one per thread for the other person,
 one per thread for the last message, one per thread for the unread count. Doing
 it here keeps that to a handful of batched queries.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.communication;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import za.ac.cput.domain.communication.ChatMedia;
import za.ac.cput.domain.communication.Conversation;
import za.ac.cput.domain.communication.ConversationParticipant;
import za.ac.cput.domain.communication.Message;
import za.ac.cput.domain.enums.ChatMediaType;
import za.ac.cput.domain.identity.User;
import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.dto.communication.ChatDtos.ChatMediaUploaded;
import za.ac.cput.dto.communication.ChatDtos.ChatMediaView;
import za.ac.cput.dto.communication.ChatDtos.ChatMessageView;
import za.ac.cput.dto.communication.ChatDtos.ChatParticipant;
import za.ac.cput.dto.communication.ChatDtos.ChatThreadView;
import za.ac.cput.factory.communication.ChatMediaFactory;
import za.ac.cput.factory.communication.ConversationFactory;
import za.ac.cput.factory.communication.ConversationParticipantFactory;
import za.ac.cput.factory.communication.MessageFactory;
import za.ac.cput.repository.communication.ChatMediaRepository;
import za.ac.cput.repository.communication.ConversationParticipantRepository;
import za.ac.cput.repository.communication.ConversationRepository;
import za.ac.cput.repository.communication.MessageRepository;
import za.ac.cput.repository.identity.UserRepository;
import za.ac.cput.repository.marketplace.ListingRepository;
import za.ac.cput.security.MediaLinkSigner;
import za.ac.cput.storage.ChatMediaStorage;

@Service
public class ChatServiceImpl implements IChatService {

    /*
     Stands in for "never opened this thread" when counting unread messages.
     ConversationParticipant.lastReadAt is null until the first read, and a null
     cannot be compared with sentAt > ?.
    */
    private static final LocalDateTime NEVER_READ = LocalDateTime.of(1970, 1, 1, 0, 0);

    private static final int PREVIEW_LENGTH = 120;

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final ChatMediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ChatMediaStorage storage;
    private final MediaLinkSigner linkSigner;
    private final NotificationPublisher notificationPublisher;
    private final long maxUploadBytes;
    private final long dailyQuotaBytes;

    public ChatServiceImpl(ConversationRepository conversationRepository,
                           ConversationParticipantRepository participantRepository,
                           MessageRepository messageRepository,
                           ChatMediaRepository mediaRepository,
                           UserRepository userRepository,
                           ListingRepository listingRepository,
                           ChatMediaStorage storage,
                           MediaLinkSigner linkSigner,
                           NotificationPublisher notificationPublisher,
                           @Value("${app.chat.media.max-bytes:26214400}") long maxUploadBytes,
                           @Value("${app.chat.media.daily-quota-bytes:209715200}") long dailyQuotaBytes) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.storage = storage;
        this.linkSigner = linkSigner;
        this.notificationPublisher = notificationPublisher;
        this.maxUploadBytes = maxUploadBytes;
        this.dailyQuotaBytes = dailyQuotaBytes;
    }

    @Override
    public List<ChatThreadView> threadsFor(long userId) {
        List<ConversationParticipant> mine = this.participantRepository.findByUserId(userId);
        if (mine.isEmpty()) {
            return List.of();
        }

        List<Long> conversationIds = mine.stream()
                .map(ConversationParticipant::getConversationId)
                .toList();

        // Everything below is batched: three queries for the whole inbox rather
        // than three per thread.
        Map<Long, Message> latestByConversation = this.messageRepository
                .findLatestPerConversation(conversationIds).stream()
                .collect(Collectors.toMap(Message::getConversationId, Function.identity()));

        Map<Long, ConversationParticipant> otherByConversation = this.participantRepository
                .findByConversationIdInAndUserIdNot(conversationIds, userId).stream()
                .collect(Collectors.toMap(ConversationParticipant::getConversationId,
                        Function.identity(), (first, second) -> first));

        Map<Long, Conversation> conversations = this.conversationRepository
                .findAllById(conversationIds).stream()
                .collect(Collectors.toMap(Conversation::getConversationId, Function.identity()));

        Map<Long, User> users = usersById(otherByConversation.values().stream()
                .map(ConversationParticipant::getUserId).distinct().toList());

        Map<Long, String> listingTitles = listingTitlesById(conversations.values().stream()
                .map(Conversation::getListingId)
                .filter(Objects::nonNull)
                .distinct().toList());

        return mine.stream()
                .map(participant -> toThreadView(participant, conversations, latestByConversation,
                        otherByConversation, users, listingTitles))
                .sorted(Comparator.comparing(
                        (ChatThreadView view) -> view.lastMessageAt() == null
                                ? LocalDateTime.MIN
                                : view.lastMessageAt())
                        .reversed())
                .toList();
    }

    @Transactional
    @Override
    public ChatThreadView startThread(long userId, long otherUserId, Long listingId) {
        if (userId == otherUserId) {
            throw new IllegalArgumentException("Chat: you cannot start a conversation with yourself");
        }
        if (!this.userRepository.existsById(otherUserId)) {
            throw new IllegalArgumentException("Chat: that student does not exist");
        }

        long conversationId = this.participantRepository
                .findSharedConversationId(userId, otherUserId, listingId)
                .orElseGet(() -> createConversation(userId, otherUserId, listingId));

        return threadsFor(userId).stream()
                .filter(view -> view.conversationId() == conversationId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chat: thread vanished after creation"));
    }

    @Override
    public List<ChatMessageView> messagesIn(long userId, long conversationId, long afterMessageId) {
        requireParticipant(userId, conversationId);

        List<Message> messages = this.messageRepository
                .findByConversationIdAndMessageIdGreaterThanOrderByMessageIdAsc(
                        conversationId, afterMessageId);
        return withMedia(messages, userId);
    }

    @Transactional
    @Override
    public ChatMessageView sendMessage(long userId, long conversationId, String content, Long mediaId) {
        requireParticipant(userId, conversationId);

        ChatMedia media = null;
        if (mediaId != null) {
            media = this.mediaRepository.findById(mediaId).orElseThrow(
                    () -> new IllegalArgumentException("Chat: that attachment does not exist"));

            // The upload must belong to this person AND to this thread. Without both
            // checks a student could attach someone else's upload, or replay an
            // upload from one conversation into another.
            if (media.getUploaderId() != userId || media.getConversationId() != conversationId) {
                throw new AccessDeniedException("Chat: that attachment is not yours to send");
            }
            if (media.getMessageId() != null) {
                throw new IllegalArgumentException("Chat: that attachment has already been sent");
            }
        }

        Message saved = this.messageRepository.save(
                MessageFactory.createMediaMessage(conversationId, userId, content, media != null));

        if (media != null) {
            media = this.mediaRepository.save(
                    ChatMediaFactory.attachToMessage(media, saved.getMessageId()));
        }

        // The sender has by definition read their own message; without this their
        // own send would come back as unread on the next poll.
        touchLastRead(userId, conversationId);

        notifyOtherParticipants(conversationId, userId, saved.getContent(), media != null);

        return toMessageView(saved, media, userId);
    }

    @Transactional
    @Override
    public ChatMediaUploaded uploadMedia(long userId, long conversationId,
                                         MultipartFile file, Integer durationMs) {
        requireParticipant(userId, conversationId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chat: no file was uploaded");
        }
        if (file.getSize() > this.maxUploadBytes) {
            throw new IllegalArgumentException(
                    "Chat: attachments are limited to " + (this.maxUploadBytes / (1024 * 1024)) + " MB");
        }

        // Stops one student filling the disk - on App Service a full /home takes the
        // whole app down, not just uploads.
        long usedToday = this.mediaRepository.sumSizeBytesUploadedSince(
                userId, LocalDateTime.now().minus(Duration.ofDays(1)));
        if (usedToday + file.getSize() > this.dailyQuotaBytes) {
            throw new IllegalArgumentException("Chat: you have reached today's upload limit");
        }

        String declaredType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase().split(";")[0].trim();

        String extension = this.storage.extensionFor(declaredType);
        ChatMediaType mediaType = this.storage.mediaTypeFor(declaredType);
        if (extension == null || mediaType == null) {
            throw new IllegalArgumentException("Chat: that file type is not supported");
        }

        try {
            if (!this.storage.contentMatchesType(file, declaredType)) {
                throw new IllegalArgumentException("Chat: that file does not look like a " + declaredType);
            }

            String storageKey = this.storage.save(file, extension);

            ChatMedia saved = this.mediaRepository.save(ChatMediaFactory.createChatMedia(
                    conversationId, userId, storageKey, declaredType, mediaType,
                    file.getSize(), durationMs, file.getOriginalFilename()));

            return new ChatMediaUploaded(saved.getChatMediaId(), saved.getMediaType(), saved.getDurationMs());
        } catch (IOException e) {
            throw new IllegalStateException("Chat: the upload could not be saved", e);
        }
    }

    @Transactional
    @Override
    public void markRead(long userId, long conversationId) {
        requireParticipant(userId, conversationId);
        touchLastRead(userId, conversationId);
    }

    @Override
    public long unreadCountFor(long userId) {
        return this.participantRepository.findByUserId(userId).stream()
                .mapToLong(participant -> unreadFor(participant, userId))
                .sum();
    }

    @Override
    public ChatMedia mediaForViewer(long mediaId, long viewerId) {
        ChatMedia media = this.mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            return null;
        }
        /*
         Re-checked on every request rather than trusted from the signature. That is
         what makes removing someone from a thread take effect immediately instead
         of whenever their last signed link happens to expire.
        */
        if (!this.participantRepository.existsByConversationIdAndUserId(
                media.getConversationId(), viewerId)) {
            return null;
        }
        return media;
    }

    // ---- internals ----

    private long createConversation(long userId, long otherUserId, Long listingId) {
        Conversation conversation = this.conversationRepository.save(
                ConversationFactory.createConversation(listingId));

        this.participantRepository.save(ConversationParticipantFactory
                .createConversationParticipant(conversation.getConversationId(), userId));
        this.participantRepository.save(ConversationParticipantFactory
                .createConversationParticipant(conversation.getConversationId(), otherUserId));

        return conversation.getConversationId();
    }

    /**
     * Tells everyone else in the thread that a message arrived.
     *
     * The notification carries the CONVERSATION id, not the message id, because
     * that is what the frontend's notificationRoute needs to build
     * /messages/{id}.
     */
    private void notifyOtherParticipants(long conversationId, long senderId,
                                         String content, boolean hasAttachment) {
        String senderName = this.userRepository.findById(senderId)
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("A student");

        String preview = hasAttachment && (content == null || content.isBlank())
                ? "Sent an attachment"
                : truncate(content);

        this.participantRepository.findByConversationId(conversationId).stream()
                .filter(participant -> participant.getUserId() != senderId)
                .forEach(participant -> this.notificationPublisher.messageReceived(
                        participant.getUserId(), senderName, preview, conversationId));
    }

    private void requireParticipant(long userId, long conversationId) {
        if (!this.participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            // Same response whether the thread is someone else's or does not exist,
            // so ids cannot be probed for existence.
            throw new AccessDeniedException("Chat: you are not part of that conversation");
        }
    }

    private void touchLastRead(long userId, long conversationId) {
        this.participantRepository.findByConversationIdAndUserId(conversationId, userId)
                .ifPresent(participant -> this.participantRepository.save(
                        new ConversationParticipant.Builder()
                                .copy(participant)
                                .setLastReadAt(LocalDateTime.now())
                                .build()));
    }

    private long unreadFor(ConversationParticipant participant, long userId) {
        LocalDateTime since = participant.getLastReadAt() == null
                ? NEVER_READ
                : participant.getLastReadAt();
        return this.messageRepository.countByConversationIdAndSenderIdNotAndSentAtAfter(
                participant.getConversationId(), userId, since);
    }

    private List<ChatMessageView> withMedia(List<Message> messages, long viewerId) {
        if (messages.isEmpty()) {
            return List.of();
        }

        Map<Long, ChatMedia> mediaByMessage = this.mediaRepository
                .findByMessageIdIn(messages.stream().map(Message::getMessageId).toList())
                .stream()
                .collect(Collectors.toMap(ChatMedia::getMessageId, Function.identity(),
                        (first, second) -> first));

        return messages.stream()
                .map(message -> toMessageView(message, mediaByMessage.get(message.getMessageId()), viewerId))
                .toList();
    }

    private ChatMessageView toMessageView(Message message, ChatMedia media, long viewerId) {
        return new ChatMessageView(
                message.getMessageId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getContent(),
                message.getSentAt(),
                media == null ? null : toMediaView(media, viewerId));
    }

    private ChatMediaView toMediaView(ChatMedia media, long viewerId) {
        return new ChatMediaView(
                media.getChatMediaId(),
                media.getMediaType(),
                media.getMimeType(),
                this.linkSigner.urlFor(media.getChatMediaId(), viewerId),
                media.getDurationMs(),
                media.getSizeBytes(),
                media.getOriginalFilename());
    }

    private ChatThreadView toThreadView(ConversationParticipant mine,
                                        Map<Long, Conversation> conversations,
                                        Map<Long, Message> latestByConversation,
                                        Map<Long, ConversationParticipant> otherByConversation,
                                        Map<Long, User> users,
                                        Map<Long, String> listingTitles) {
        long conversationId = mine.getConversationId();
        Conversation conversation = conversations.get(conversationId);
        Message latest = latestByConversation.get(conversationId);
        ConversationParticipant other = otherByConversation.get(conversationId);
        User otherUser = other == null ? null : users.get(other.getUserId());
        Long listingId = conversation == null ? null : conversation.getListingId();

        return new ChatThreadView(
                conversationId,
                listingId,
                listingId == null ? null : listingTitles.get(listingId),
                otherUser == null
                        ? null
                        : new ChatParticipant(otherUser.getUserId(), otherUser.getFirstName(),
                                otherUser.getLastName()),
                preview(latest),
                latest == null ? null : latest.getSentAt(),
                unreadFor(mine, mine.getUserId()));
    }

    /*
     A media-only message stores "" as its content, so fall back to a label rather
     than showing an empty inbox row.
    */
    private static String preview(Message latest) {
        if (latest == null) {
            return null;
        }
        String content = latest.getContent();
        return content == null || content.isBlank() ? "Attachment" : truncate(content);
    }

    /** Keeps a preview inside the notification column and readable in a list row. */
    private static String truncate(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > PREVIEW_LENGTH
                ? content.substring(0, PREVIEW_LENGTH) + "..."
                : content;
    }

    private Map<Long, User> usersById(List<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        Map<Long, User> byId = new HashMap<>();
        this.userRepository.findAllById(userIds).forEach(user -> byId.put(user.getUserId(), user));
        return byId;
    }

    private Map<Long, String> listingTitlesById(List<Long> listingIds) {
        if (listingIds.isEmpty()) return Map.of();
        Map<Long, String> byId = new HashMap<>();
        this.listingRepository.findAllById(listingIds)
                .forEach(listing -> byId.put(listing.getListingId(), listing.getTitle()));
        return byId;
    }

}
