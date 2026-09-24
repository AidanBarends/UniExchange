/*
 GlobalExceptionHandler.java

 Turns factory validation failures into 400 responses. Without this every
 IllegalArgumentException thrown by a *Factory would surface as a 500.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import za.ac.cput.exception.ConflictException;
import za.ac.cput.exception.InsufficientFundsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Request validation failed");

        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        body.put("fields", fields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /*
     Must sit before the AuthenticationException handler in intent: DisabledException
     IS an AuthenticationException, so without this a student whose email is simply
     unverified would be told "Invalid email or password" and have no way forward.
     Spring picks the most specific handler, so both can coexist.

     Trade-off accepted: distinguishing "not verified" from "wrong password" is a
     mild account-enumeration signal. For a campus marketplace the usable flow is
     worth more than hiding it.
    */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleUnverified(DisabledException ex) {
        Map<String, Object> body = base(HttpStatus.FORBIDDEN,
                "Verify your student email before signing in.");
        body.put("code", "EMAIL_NOT_VERIFIED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleUnavailable(IllegalStateException ex) {
        // Raised when the verification email could not be delivered.
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    /*
     409, not 400: the request was well formed, it just lost a race - the listing
     was bought by someone else, or this transaction was already confirmed. The
     code tells the frontend which message to show.
    */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        Map<String, Object> body = base(HttpStatus.CONFLICT, ex.getMessage());
        body.put("code", ex.getCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientFunds(InsufficientFundsException ex) {
        Map<String, Object> body = base(HttpStatus.CONFLICT,
                "You do not have enough in your wallet for this purchase.");
        body.put("code", "INSUFFICIENT_FUNDS");
        body.put("balance", ex.getBalance());
        body.put("required", ex.getRequired());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /*
     Without this, an oversized upload produces Tomcat's HTML error page with a
     500 status. The frontend's safeJson() then shows that raw HTML to the
     student as the error message. Note this only reaches us if
     server.tomcat.max-swallow-size allows the rest of the body to be read -
     see application.properties.
    */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
        // CONTENT_TOO_LARGE, not PAYLOAD_TOO_LARGE - the latter is deprecated in Spring 7.
        return build(HttpStatus.CONTENT_TOO_LARGE, "That file is too large.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "You are not allowed to perform this action");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(base(status, message));
    }

    private Map<String, Object> base(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

}
