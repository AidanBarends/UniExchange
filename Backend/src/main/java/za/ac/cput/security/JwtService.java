/*
 JwtService.java

 Mints and verifies HS256 JSON Web Tokens.

 Uses the non-deprecated jjwt 0.12+/0.13 API only: subject()/issuedAt()/expiration()
 rather than setSubject()/setIssuedAt()/setExpiration(), signWith(SecretKey) rather
 than signWith(key, SignatureAlgorithm), and
 Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload() rather than
 setSigningKey()/parseClaimsJws()/getBody().

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlSeconds;
    private final long rememberedTtlSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.ttl-seconds:3600}") long ttlSeconds,
                      @Value("${app.jwt.remembered-ttl-seconds:2592000}") long rememberedTtlSeconds) {
        // Throws the unchecked WeakKeyException if the secret is under 32 bytes (HS256).
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
        this.rememberedTtlSeconds = rememberedTtlSeconds;
    }

    public String generateToken(String subject) {
        return generateToken(subject, Map.of());
    }

    public String generateToken(String subject, Map<String, Object> extraClaims) {
        return generateToken(subject, extraClaims, this.ttlSeconds);
    }

    public String generateToken(String subject, Map<String, Object> extraClaims, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(this.key)
                .compact();
    }

    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(this.key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) throws JwtException {
        return parseClaims(token).getSubject();
    }

    public long getTtlSeconds() {
        return this.ttlSeconds;
    }

    /*
     How long a "Remember me" sign-in lasts. Deliberately much longer than the
     default: with no refresh endpoint, an hour-long token would sign the student
     out every hour no matter what they ticked.

     The trade this accepts: a JWT cannot be revoked server-side, so a stolen
     remembered token stays valid until it expires. Shorten
     app.jwt.remembered-ttl-seconds if that is not an acceptable trade.
    */
    public long getRememberedTtlSeconds() {
        return this.rememberedTtlSeconds;
    }

    /** The lifetime to mint with, given whether "Remember me" was ticked. */
    public long ttlSecondsFor(boolean remembered) {
        return remembered ? this.rememberedTtlSeconds : this.ttlSeconds;
    }

}
