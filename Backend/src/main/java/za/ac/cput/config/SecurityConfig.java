/*
 SecurityConfig.java

 Stateless JWT security for Spring Security 7 (shipped with Spring Boot 4).

 Three Boot 3 idioms that no longer work here, for future reference:
   - DaoAuthenticationProvider has no no-arg constructor and no
     setUserDetailsService(); the UserDetailsService is constructor-injected.
   - HttpSecurity.build() no longer declares "throws Exception", so the
     SecurityFilterChain bean needs no throws clause.
   - Only the Customizer lambda overloads exist - there is no .and() chaining.

 @EnableWebSecurity is deliberately absent: Boot 4's ServletWebSecurityAutoConfiguration
 already applies it.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.config;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import za.ac.cput.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Without this a request carrying no (or a bad) token gets 403 from the
                // default Http403ForbiddenEntryPoint. For a REST API 401 is correct:
                // "you are not authenticated", leaving 403 to mean "authenticated but
                // not permitted".
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Access denial calls response.sendError, which re-enters this
                        // filter chain as an ERROR dispatch to /error. That dispatch
                        // carries no authentication, so without this line the entry
                        // point above would overwrite every 403 with a 401.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/listings/**",
                                "/api/listing-images/**",
                                "/api/categories/**",
                                "/api/campuses/**",
                                "/api/bulletin-posts/**",
                                "/api/bulletin-post-images/**",
                                "/uploads/**",
                                // A seller's rating and badge are shown on every listing card
                                // and on public profiles, so they must be readable signed-out.
                                // Writing a review is POST, which falls through to the
                                // ADMIN/authenticated rules below.
                                "/api/reviews/reviewee/**",
                                "/api/trusted-seller-badges/user/**").permitAll()
                        /*
                         Chat attachments. permitAll here is not "public": <img>, <audio>
                         and <video> cannot send an Authorization header, so a filter-chain
                         rule would 401 every media tag on the page. ChatMediaController
                         instead verifies an HMAC signature bound to (mediaId, viewerId,
                         expiry) AND re-checks conversation participation on every request,
                         which is strictly stronger than "any signed-in student".
                        */
                        .requestMatchers(HttpMethod.GET, "/api/chat/media/**").permitAll()
                        /*
                         PayFast's server-to-server callback carries no JWT. Without this it
                         would 401 on every delivery, PayFast would retry forever, and no
                         top-up would ever be credited. The handler authenticates the caller
                         itself: signature, source IP, and a confirmation POST back to PayFast.
                        */
                        .requestMatchers(HttpMethod.POST, "/api/payfast/itn").permitAll()
                        .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reports/**").hasRole("ADMIN")
                        /*
                         The generic CRUD controllers for money, chat and trust are ADMIN-only.
                         They take ids straight from the request body with no ownership check,
                         so while merely "authenticated" any student could credit their own
                         wallet, read anyone's private messages, forge reviews or grant
                         themselves a Trusted Seller badge.

                         Real use goes through the authorization-aware flow controllers
                         instead: /api/chat, /api/wallet, /api/purchases, and POST /api/reviews.
                         Keep new endpoints out of these prefixes.
                        */
                        .requestMatchers("/api/wallets/**",
                                "/api/wallet-transactions/**",
                                "/api/payments/**",
                                "/api/transactions/**",
                                "/api/conversations/**",
                                "/api/conversation-participants/**",
                                "/api/messages/**",
                                "/api/trusted-seller-badges/**").hasRole("ADMIN")
                        // Reviews: anyone signed in may POST one (ReviewController checks they
                        // were party to a COMPLETED transaction); editing and deleting are ADMIN.
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Declaring this bean makes Boot's UserDetailsServiceAutoConfiguration back off,
    // so the random generated password no longer appears at startup.
    //
    // The DaoAuthenticationProvider is built here rather than exposed as its own
    // @Bean on purpose: an AuthenticationProvider bean makes Spring Security log a
    // warning that the UserDetailsService bean will be ignored for global auth,
    // which is misleading - it is wired straight into the provider below.
    //
    // Note the Security 7 signature: UserDetailsService is constructor-injected.
    // setUserDetailsService() was removed and there is no no-arg constructor.
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(this.allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        /*
         Range is here so a range-aware fetch() stays possible. Media elements
         (<img>/<audio>/<video> without crossorigin) issue no-CORS requests and are
         unaffected either way, but the moment anything reads media through fetch or
         adds crossorigin="anonymous", a missing Range entry turns into a preflight
         403 with an unhelpful console message. The exposed range headers are what
         let script see Content-Range/Accept-Ranges on the response.
        */
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Range"));
        config.setExposedHeaders(List.of("Authorization", "Accept-Ranges", "Content-Range", "Content-Length"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
