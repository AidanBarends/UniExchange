/*
 AuthController.java

 Registration, email-OTP verification, login and "who am I".

 The verification gate is the point of this class. Registration creates a
 PENDING_VERIFICATION account and emails a short code; it returns NO token. Only
 /verify-otp - which requires the code that was delivered to the
 @mycput.ac.za mailbox - issues a JWT. So a made-up student number cannot obtain
 credentials: it never receives the code.

 The code is also a second factor at sign-in, not just at registration. /login
 has two outcomes once the password checks out:

   trusted device  -> 200 with an AuthResponse, exactly as before.
   unknown device  -> 202 with a RegistrationResponse: a code has been sent, and
                      the client must finish at /verify-otp.

 A device becomes trusted only by completing /verify-otp, and the token proving
 it never replaces the password - it only ever skips the second factor. See
 DeviceTrustService.

 Every entity here (User, Role, UserRole, Verification, TrustedDevice) is built
 through its factory, never through setters.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.identity;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.enums.RoleType;
import za.ac.cput.domain.identity.Role;
import za.ac.cput.domain.identity.User;
import za.ac.cput.dto.auth.AuthResponse;
import za.ac.cput.dto.auth.LoginRequest;
import za.ac.cput.dto.auth.RegisterRequest;
import za.ac.cput.dto.auth.RegistrationResponse;
import za.ac.cput.dto.auth.ResendOtpRequest;
import za.ac.cput.dto.auth.VerifyOtpRequest;
import za.ac.cput.factory.identity.RoleFactory;
import za.ac.cput.factory.identity.UserFactory;
import za.ac.cput.mail.EmailSender;
import za.ac.cput.security.JwtService;
import za.ac.cput.security.UniExchangeUserDetailsService.AuthenticatedUser;
import za.ac.cput.service.identity.DeviceTrustService;
import za.ac.cput.service.identity.IRoleService;
import za.ac.cput.service.identity.IUserRoleService;
import za.ac.cput.service.identity.IUserService;
import za.ac.cput.service.identity.OtpService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final IUserService userService;
    private final IRoleService roleService;
    private final IUserRoleService userRoleService;
    private final OtpService otpService;
    private final DeviceTrustService deviceTrustService;
    private final EmailSender emailSender;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(IUserService userService,
                          IRoleService roleService,
                          IUserRoleService userRoleService,
                          OtpService otpService,
                          DeviceTrustService deviceTrustService,
                          EmailSender emailSender,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.otpService = otpService;
        this.deviceTrustService = deviceTrustService;
        this.emailSender = emailSender;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Creates a pending account and emails a code. Returns no token by design. */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        String email = normalise(request.email());

        if (this.userService.existsByEmail(email)) {
            throw new IllegalArgumentException("An account already exists for " + email);
        }

        User created = this.userService.create(UserFactory.createUser(
                email,
                request.firstName(),
                request.middleName(),
                request.lastName(),
                request.cellPhone(),
                this.passwordEncoder.encode(request.password()),
                request.dateOfBirth(),
                AccountStatus.PENDING_VERIFICATION,
                request.campusId()));

        this.userRoleService.assignRole(created.getUserId(), defaultRole().getRoleId());

        sendCode(created);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RegistrationResponse(
                email,
                "We sent a %d-digit code to %s. Enter it to activate your account."
                        .formatted(6, email),
                this.otpService.getTtlMinutes() * 60));
    }

    /**
     * Exchanges a valid code for a token, activating the account, and trusts the
     * browser it came from. This is the ONLY place a device becomes trusted -
     * which is what makes "skip the OTP" safe: it can only ever be earned by
     * completing an OTP.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        User user = this.userService.findByEmail(normalise(request.email()));
        if (user == null) {
            // Same message as a wrong code, so this cannot be used to discover
            // which student numbers are registered.
            throw new IllegalArgumentException("That code is not valid. Request a new one.");
        }

        OtpService.Result result = this.otpService.check(user.getUserId(), request.code());
        switch (result) {
            case OK -> { /* fall through to activation below */ }
            case EXPIRED -> throw new IllegalArgumentException(
                    "That code has expired. Request a new one.");
            case TOO_MANY_ATTEMPTS -> throw new IllegalArgumentException(
                    "Too many incorrect attempts. Request a new code.");
            case NO_PENDING_CODE -> throw new IllegalArgumentException(
                    "There is no code waiting for this account. Request a new one.");
            case MISMATCH -> throw new IllegalArgumentException(
                    "That code is not valid. Check it and try again.");
        }

        // Already-verified accounts reach here on an ordinary sign-in, where the
        // code is a second factor rather than an activation. verifyEmail is
        // idempotent enough to re-run, but there is no reason to.
        User active = user.getEmailVerifiedAt() == null
                ? this.userService.update(UserFactory.verifyEmail(user))
                : user;

        String deviceToken = this.deviceTrustService.issue(
                active.getUserId(), userAgent, request.rememberMe());

        // Verifying logs the student straight in - no second trip to /login.
        return ResponseEntity.ok(
                tokenFor(active, rolesFor(active), request.rememberMe(), deviceToken));
    }

    /** Issues a replacement code, subject to a cooldown. */
    @PostMapping("/resend-otp")
    public ResponseEntity<RegistrationResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        String email = normalise(request.email());
        User user = this.userService.findByEmail(email);

        /*
         Both kinds of pending code land here: activating a new account, and the
         second factor for an established one signing in from a new device.
         Limiting this to PENDING_VERIFICATION would leave "Send a new code" dead
         on the sign-in path, which is the more common one now.

         SUSPENDED and DEACTIVATED accounts are deliberately excluded - they must
         not be able to pull a code at all.
        */
        boolean mayReceiveCode = user != null
                && (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION
                        || user.getAccountStatus() == AccountStatus.ACTIVE);

        if (mayReceiveCode) {
            long wait = this.otpService.resendCooldownRemaining(user.getUserId());
            if (wait > 0) {
                throw new IllegalArgumentException(
                        "Please wait %d seconds before requesting another code.".formatted(wait));
            }
            sendCode(user);
        }
        else {
            // Unknown or closed account: say nothing either way, so the endpoint
            // cannot be used to enumerate registered student numbers.
            log.info("Resend requested for {} - no eligible account, responding generically", email);
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RegistrationResponse(
                email,
                "If that account needs a code, a new one is on its way.",
                this.otpService.getTtlMinutes() * 60));
    }

    /**
     * Signs in. The password is always required; the emailed code is required on
     * top of it unless this browser has been trusted before.
     *
     * @return 200 with a token when the device is trusted, or 202 with no token
     *         when a code has just been sent and /verify-otp must finish the job.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        // Unverified accounts fail here as a DisabledException, which
        // GlobalExceptionHandler turns into 403 EMAIL_NOT_VERIFIED so the client
        // can send the student to the code screen. Bad credentials -> 401.
        //
        // This runs FIRST, so a wrong password never triggers an email. Otherwise
        // /login would be a free way to spam any student's inbox.
        Authentication authentication = this.authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalise(request.email()), request.password()));

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        User user = principal.getUser();

        // Spring Security 7 also grants authentication-factor authorities such as
        // FACTOR_PASSWORD. Those are not application roles, so keep only ROLE_*.
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();

        if (this.deviceTrustService.isTrusted(user.getUserId(), request.deviceToken())) {
            // Known browser: password alone is enough. The student may have
            // changed their mind about "Remember me" though, so let the device
            // catch up - realign returns a replacement token only when it did.
            String replacement = this.deviceTrustService.realign(
                    user.getUserId(), request.deviceToken(), userAgent, request.rememberMe());

            return ResponseEntity.ok(tokenFor(user, roles, request.rememberMe(), replacement));
        }

        log.info("Login from an untrusted device for userId {} - sending a code", user.getUserId());
        sendCode(user);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RegistrationResponse(
                user.getEmail(),
                "We sent a code to %s to confirm it's you.".formatted(user.getEmail()),
                this.otpService.getTtlMinutes() * 60));
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(principal.getUser());
    }

    /** Issues a code and emails it. The plaintext never leaves this method. */
    private void sendCode(User user) {
        String code = this.otpService.issue(user.getUserId());

        try {
            this.emailSender.send(
                    user.getEmail(),
                    "Your UniExchange verification code",
                    """
                    Hi %s,

                    Your UniExchange verification code is:

                        %s

                    It expires in %d minutes.

                    If you did not sign up for UniExchange or try to sign in just
                    now, you can ignore this email - someone may have typed your
                    address by mistake. Nobody can get into your account with this
                    code alone; your password is still needed.

                    - The UniExchange team
                    """.formatted(user.getFirstName(), code, this.otpService.getTtlMinutes()));
        }
        catch (MailException ex) {
            log.error("Could not deliver the verification code to {}", user.getEmail(), ex);
            throw new IllegalStateException(
                    "We could not send the verification email. Please try again shortly.", ex);
        }
    }

    private List<String> rolesFor(User user) {
        return this.userRoleService.findByUserId(user.getUserId()).stream()
                .map(userRole -> this.roleService.read(userRole.getRoleId()))
                .filter(role -> role != null)
                .map(role -> "ROLE_" + role.getName().name())
                .toList();
    }

    /** The STUDENT role every new account starts with, created on first use. */
    private Role defaultRole() {
        Role existing = this.roleService.findByName(RoleType.STUDENT);
        if (existing != null) {
            return existing;
        }
        return this.roleService.create(RoleFactory.createRole(
                RoleType.STUDENT, "Default role for a registered student"));
    }

    /**
     * Builds the signed-in response.
     *
     * @param remembered  "Remember me" was ticked, so the session lasts weeks
     *                    rather than an hour. Without this the checkbox would
     *                    only skip the code and still sign the student out
     *                    hourly, since there is no refresh endpoint.
     * @param deviceToken the freshly minted device token, or null when the
     *                    browser already holds one.
     */
    private AuthResponse tokenFor(User user, List<String> roles,
                                  boolean remembered, String deviceToken) {
        long ttlSeconds = this.jwtService.ttlSecondsFor(remembered);

        String token = this.jwtService.generateToken(user.getEmail(), Map.of(
                "uid", user.getUserId(),
                "roles", roles), ttlSeconds);

        return new AuthResponse(
                token,
                "Bearer",
                ttlSeconds,
                user.getUserId(),
                user.getEmail(),
                roles,
                deviceToken);
    }

    /** Student addresses are case-insensitive; store and compare them lowercased. */
    private static String normalise(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
