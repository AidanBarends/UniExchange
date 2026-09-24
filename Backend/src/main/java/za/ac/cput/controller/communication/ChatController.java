/*
 ChatController.java

 The chat API the frontend actually uses: /api/chat.

 The generic CRUD controllers for Conversation, ConversationParticipant and
 Message still exist but are ADMIN-only (see SecurityConfig), because they take
 ids straight from the request body and would let any signed-in student read or
 post into anyone's thread. Everything here resolves the acting student from the
 JWT instead - a conversationId in the URL is never enough on its own.

 Delivery is by polling rather than WebSockets. See the plan for the reasoning;
 the short version is that the app is deployed to Azure App Service, where cheap
 tiers unload the app after ~20 minutes idle and drop every persistent
 connection, and where scaling out would break an in-memory STOMP broker. The
 afterMessageId cursor keeps each poll cheap and is the same shape a push
 implementation would use later.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.controller.communication;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import za.ac.cput.dto.communication.ChatDtos.ChatMediaUploaded;
import za.ac.cput.dto.communication.ChatDtos.ChatMessageView;
import za.ac.cput.dto.communication.ChatDtos.ChatThreadView;
import za.ac.cput.dto.communication.ChatDtos.SendMessageRequest;
import za.ac.cput.dto.communication.ChatDtos.StartThreadRequest;
import za.ac.cput.dto.communication.ChatDtos.UnreadCount;
import za.ac.cput.security.UniExchangeUserDetailsService.AuthenticatedUser;
import za.ac.cput.service.communication.IChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final IChatService service;

    public ChatController(IChatService service) {
        this.service = service;
    }

    /** The inbox: every thread, newest activity first, with previews and unread counts. */
    @GetMapping("/threads")
    public List<ChatThreadView> threads(@AuthenticationPrincipal AuthenticatedUser principal) {
        return this.service.threadsFor(me(principal));
    }

    /**
     * Opens the conversation with another student, creating it only if there is not
     * already one about the same listing. This is what the "Message Seller" button
     * on a listing calls.
     */
    @PostMapping("/threads")
    public ResponseEntity<ChatThreadView> startThread(
            @RequestBody StartThreadRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                this.service.startThread(me(principal), request.otherUserId(), request.listingId()));
    }

    /**
     * The poll. afterMessageId is the highest id the client already has; 0 loads the
     * whole thread. Returns an empty list when nothing is new, which is the common
     * case and deliberately cheap.
     */
    @GetMapping("/threads/{conversationId}/messages")
    public List<ChatMessageView> messages(
            @PathVariable long conversationId,
            @RequestParam(defaultValue = "0") long afterMessageId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return this.service.messagesIn(me(principal), conversationId, afterMessageId);
    }

    @PostMapping("/threads/{conversationId}/messages")
    public ResponseEntity<ChatMessageView> send(
            @PathVariable long conversationId,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.sendMessage(
                me(principal), conversationId, request.content(), request.mediaId()));
    }

    /**
     * Uploads an attachment and returns its id; the client then sends a message
     * carrying that id.
     *
     * Two steps rather than one multipart request that also creates the message,
     * because it lets the UI show upload progress, preview the file before
     * committing to send, and retry a failed upload without the student re-picking
     * the file or losing a typed caption.
     *
     * durationMs is supplied by the browser for voice notes: MediaRecorder writes a
     * streaming container with no duration in it, so the blob reports Infinity and
     * the value has to be timed during recording instead. It is untrusted and is
     * clamped by ChatMediaFactory.
     */
    @PostMapping("/threads/{conversationId}/media")
    public ResponseEntity<ChatMediaUploaded> uploadMedia(
            @PathVariable long conversationId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Integer durationMs,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                this.service.uploadMedia(me(principal), conversationId, file, durationMs));
    }

    @PostMapping("/threads/{conversationId}/read")
    public ResponseEntity<Void> markRead(@PathVariable long conversationId,
                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        this.service.markRead(me(principal), conversationId);
        return ResponseEntity.noContent().build();
    }

    /** Drives the dot on the Messages nav item; polled far less often than a thread. */
    @GetMapping("/unread-count")
    public UnreadCount unreadCount(@AuthenticationPrincipal AuthenticatedUser principal) {
        return new UnreadCount(this.service.unreadCountFor(me(principal)));
    }

    /*
     Guarded rather than dereferenced straight away. The JWT filter always puts an
     AuthenticatedUser in the context, so on the normal path this never triggers -
     but if the principal is ever absent or some other type, a bare
     principal.getUser() throws NullPointerException and the student gets a 500
     with a stack trace where they should get a plain "sign in again". Same
     defensive shape AuthController.me() already uses.
    */
    private static long me(AuthenticatedUser principal) {
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("You must be signed in to use chat");
        }
        return principal.getUser().getUserId();
    }

}
