/*
 ChatAuthorizationTest.java

 The first controller-layer tests in this project. They exist because the
 interesting failure mode of chat is not "does it work" but "can student C read
 a thread between A and B" - and that is decided in the controller/service, not
 in a factory.

 @SpringBootTest + MockMvc rather than @WebMvcTest, so the real SecurityConfig,
 the real JwtAuthenticationFilter and the real service run. A @WebMvcTest with a
 mocked service would happily pass while the actual authorization was missing.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.dto.communication.ChatDtos.ChatMessageView;
import za.ac.cput.dto.communication.ChatDtos.ChatThreadView;
import za.ac.cput.factory.identity.UserFactory;
import za.ac.cput.mail.EmailSender;
import za.ac.cput.repository.identity.UserRepository;
import za.ac.cput.security.UniExchangeUserDetailsService;
import za.ac.cput.service.communication.IChatService;

@SpringBootTest
class ChatAuthorizationTest {

    private static final AtomicInteger NEXT_USER_ID = new AtomicInteger(600_000);

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private IChatService chatService;

    /** Nothing here sends mail; this just keeps SMTP out of the test context. */
    @MockitoBean
    private EmailSender emailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniExchangeUserDetailsService userDetailsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // .apply(springSecurity()) is essential and easy to forget: without it
        // MockMvc wires up the controllers but NOT the Spring Security filter
        // chain, so every request sails through unauthenticated and the endpoint
        // answers 200. A security test built that way passes no matter how wide
        // open the application really is.
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();
    }

    /**
     * A real, persisted student.
     *
     * Chat verifies the other party exists before opening a thread, so an invented
     * id is not enough here - unlike the money tests, where foreign keys are plain
     * scalars with nothing to check against.
     */
    private long freshUser() {
        int n = NEXT_USER_ID.incrementAndGet();
        User saved = this.userRepository.save(UserFactory.createUser(
                n + "@mycput.ac.za", "Test", null, "Student" + n, null,
                "not-a-real-hash", LocalDate.of(2000, 1, 1),
                AccountStatus.ACTIVE, null));
        return saved.getUserId();
    }

    // ---- filter-chain level: no token at all ----

    @Test
    void chatRequiresAToken() throws Exception {
        this.mockMvc.perform(get("/api/chat/threads")).andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/api/chat/threads/1/messages")).andExpect(status().isUnauthorized());
        this.mockMvc.perform(post("/api/chat/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otherUserId\":2}"))
                .andExpect(status().isUnauthorized());
    }

    /*
     These are the generic CRUD controllers. Before this work they were merely
     "authenticated", which meant any signed-in student could read anyone's
     private messages or credit their own wallet. They are ADMIN-only now, and an
     anonymous caller must not reach them either.
    */
    @Test
    void theGenericCrudEndpointsAreNotOpen() throws Exception {
        this.mockMvc.perform(get("/api/messages/conversation/1")).andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/api/conversation-participants/user/1")).andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/api/wallets")).andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/api/wallet-transactions")).andExpect(status().isUnauthorized());
    }

    /*
     The test that actually proves the lockdown.

     The one above only shows a token is required, which was already true before
     this work - and was exactly the problem: "authenticated" was ALL these
     endpoints demanded, so any student could credit their own wallet or read
     anyone's messages. Here the caller IS signed in, as an ordinary STUDENT, and
     must still be refused.
    */
    @Test
    @WithMockUser(roles = "STUDENT")
    void aSignedInStudentStillCannotReachTheMoneyAndTrustCrud() throws Exception {
        this.mockMvc.perform(post("/api/wallets/user/1/credit").param("amount", "999999"))
                .andExpect(status().isForbidden());
        this.mockMvc.perform(get("/api/wallets")).andExpect(status().isForbidden());
        this.mockMvc.perform(get("/api/wallet-transactions")).andExpect(status().isForbidden());
        this.mockMvc.perform(get("/api/transactions")).andExpect(status().isForbidden());
        this.mockMvc.perform(get("/api/messages/conversation/1")).andExpect(status().isForbidden());
        this.mockMvc.perform(get("/api/conversation-participants/user/1")).andExpect(status().isForbidden());
        this.mockMvc.perform(post("/api/trusted-seller-badges")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":1}"))
                .andExpect(status().isForbidden());
    }

    /**
     * ...while the endpoints students are meant to use stay reachable.
     *
     * A real AuthenticatedUser, not @WithMockUser: the controllers read the acting
     * student out of the principal, and Spring's stand-in principal is a different
     * type, so @WithMockUser would exercise the guard rather than the happy path.
     */
    @Test
    void aSignedInStudentCanReachTheirOwnChat() throws Exception {
        User student = this.userRepository.findById(freshUser()).orElseThrow();

        // Built through the real UserDetailsService rather than by calling the
        // AuthenticatedUser constructor, which is package-private - and rightly so.
        // This also means the test exercises the same principal the JWT filter
        // produces in production, not a lookalike.
        var principal = this.userDetailsService.loadUserByUsername(student.getEmail());

        // 200 rather than 403: these resolve the caller from the token instead of
        // taking a userId from the request, so they are safe to expose.
        var token = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());

        this.mockMvc.perform(get("/api/chat/threads").with(authentication(token)))
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/api/chat/unread-count").with(authentication(token)))
                .andExpect(status().isOk());
    }

    /**
     * A principal of the wrong shape must produce a clean refusal, not a 500 with a
     * stack trace. @WithMockUser supplies exactly that - Spring's own User type
     * rather than ours - which is a convenient way to exercise the guard.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void aPrincipalOfTheWrongTypeIsRefusedCleanly() throws Exception {
        this.mockMvc.perform(get("/api/chat/threads")).andExpect(status().isForbidden());
        this.mockMvc.perform(get("/api/wallet")).andExpect(status().isForbidden());
    }

    /** PayFast sends no JWT. If this ever 401s, no top-up is ever credited. */
    @Test
    void thePayFastCallbackIsReachableWithoutAToken() throws Exception {
        this.mockMvc.perform(post("/api/payfast/itn")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("m_payment_id=nonexistent&payment_status=COMPLETE"))
                // 200 even for a bogus payload: anything else makes PayFast retry forever.
                .andExpect(status().isOk());
    }

    /** A seller's rating and badge appear on public listing cards. */
    @Test
    void ratingsStayPubliclyReadable() throws Exception {
        this.mockMvc.perform(get("/api/reviews/reviewee/1/average")).andExpect(status().isOk());
        this.mockMvc.perform(get("/api/reviews/reviewee/1")).andExpect(status().isOk());
    }

    @Test
    void anUnsignedMediaLinkIsRejected() throws Exception {
        // Missing params -> 400; a forged signature -> 403. Neither reveals whether
        // the media exists.
        this.mockMvc.perform(get("/api/chat/media/1")).andExpect(status().isBadRequest());
        this.mockMvc.perform(get("/api/chat/media/1")
                        .param("u", "1").param("exp", "99999999999").param("sig", "forged"))
                .andExpect(status().isForbidden());
    }

    // ---- service level: a VALID token, but for the wrong student ----

    /*
     The case the filter chain cannot catch. Student C is perfectly well
     authenticated - they just have no business in A and B's conversation.
     Knowing the id must never be enough.
    */
    @Test
    void aThirdStudentCannotReadSomeoneElsesThread() {
        long alice = freshUser();
        long bob = freshUser();
        long eve = freshUser();

        ChatThreadView thread = this.chatService.startThread(alice, bob, null);
        this.chatService.sendMessage(alice, thread.conversationId(), "Is it still available?", null);

        assertThrows(AccessDeniedException.class, () ->
                this.chatService.messagesIn(eve, thread.conversationId(), 0));
        assertThrows(AccessDeniedException.class, () ->
                this.chatService.sendMessage(eve, thread.conversationId(), "butting in", null));
        assertThrows(AccessDeniedException.class, () ->
                this.chatService.markRead(eve, thread.conversationId()));
    }

    @Test
    void aThirdStudentCannotOpenSomeoneElsesAttachment() {
        long alice = freshUser();
        long bob = freshUser();
        long eve = freshUser();

        this.chatService.startThread(alice, bob, null);

        // No such attachment, so this asserts the SHAPE of the answer: null, which
        // the controller renders as 404 - byte for byte the same response as "that
        // media exists but is not yours". That is what stops the endpoint being
        // used to discover which conversations exist.
        assertNull(this.chatService.mediaForViewer(999_999L, eve));
    }

    @Test
    void startingAThreadTwiceReusesTheSameConversation() {
        long alice = freshUser();
        long bob = freshUser();

        ChatThreadView first = this.chatService.startThread(alice, bob, null);
        ChatThreadView second = this.chatService.startThread(alice, bob, null);

        // "Message Seller" is a button people tap twice. It must not split the
        // conversation in half.
        assertEquals(first.conversationId(), second.conversationId());

        // ...and it is the same thread seen from the other side.
        ChatThreadView fromBob = this.chatService.startThread(bob, alice, null);
        assertEquals(first.conversationId(), fromBob.conversationId());
    }

    @Test
    void aThreadAboutAListingIsSeparateFromAGeneralOne() {
        long alice = freshUser();
        long bob = freshUser();

        ChatThreadView general = this.chatService.startThread(alice, bob, null);
        ChatThreadView aboutListing = this.chatService.startThread(alice, bob, 4242L);

        assertTrue(general.conversationId() != aboutListing.conversationId(),
                "a chat about a specific item should not merge into an unrelated one");
    }

    @Test
    void unreadCountsOnlyTheOtherPersonsMessages() {
        long alice = freshUser();
        long bob = freshUser();

        ChatThreadView thread = this.chatService.startThread(alice, bob, null);
        this.chatService.sendMessage(alice, thread.conversationId(), "hello", null);

        assertEquals(0, this.chatService.unreadCountFor(alice), "your own message is not unread");
        assertEquals(1, this.chatService.unreadCountFor(bob));

        this.chatService.markRead(bob, thread.conversationId());
        assertEquals(0, this.chatService.unreadCountFor(bob));
    }

    @Test
    void theCursorReturnsOnlyNewMessages() {
        long alice = freshUser();
        long bob = freshUser();

        ChatThreadView thread = this.chatService.startThread(alice, bob, null);
        this.chatService.sendMessage(alice, thread.conversationId(), "first", null);

        List<ChatMessageView> all = this.chatService.messagesIn(bob, thread.conversationId(), 0);
        assertEquals(1, all.size());

        // The common polling case: nothing new, so nothing comes back.
        long cursor = all.get(0).messageId();
        assertTrue(this.chatService.messagesIn(bob, thread.conversationId(), cursor).isEmpty());

        this.chatService.sendMessage(bob, thread.conversationId(), "second", null);
        List<ChatMessageView> fresh = this.chatService.messagesIn(alice, thread.conversationId(), cursor);
        assertEquals(1, fresh.size());
        assertEquals("second", fresh.get(0).content());
    }

    @Test
    void youCannotStartAThreadWithYourself() {
        long alice = freshUser();
        assertThrows(IllegalArgumentException.class, () ->
                this.chatService.startThread(alice, alice, null));
    }

}
