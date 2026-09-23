/*
 UniExchangeUserDetailsService.java

 Bridges the identity domain onto Spring Security's UserDetails.

 The domain models User <-> Role as a UserRole entity holding plain scalar foreign
 keys rather than a @ManyToMany, so authorities are resolved by walking that join
 manually: UserRole rows for the user -> role ids -> Role rows -> "ROLE_<RoleType>".

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.domain.identity.UserRole;
import za.ac.cput.repository.identity.RoleRepository;
import za.ac.cput.repository.identity.UserRepository;
import za.ac.cput.repository.identity.UserRoleRepository;

@Service
public class UniExchangeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public UniExchangeUserDetailsService(UserRepository userRepository,
                                         UserRoleRepository userRoleRepository,
                                         RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + email));

        return new AuthenticatedUser(user, authoritiesFor(user));
    }

    private Collection<GrantedAuthority> authoritiesFor(User user) {
        List<Long> roleIds = this.userRoleRepository.findByUserId(user.getUserId()).stream()
                .map(UserRole::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        return this.roleRepository.findAllById(roleIds).stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .toList();
    }

    /**
     * Wraps the domain User so the authenticated principal keeps its id and email,
     * which AuthController needs for /api/auth/me.
     */
    public static class AuthenticatedUser implements UserDetails {

        private final User user;
        private final Collection<GrantedAuthority> authorities;

        AuthenticatedUser(User user, Collection<GrantedAuthority> authorities) {
            this.user = user;
            this.authorities = authorities;
        }

        public User getUser() {
            return this.user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.authorities;
        }

        @Override
        public String getPassword() {
            return this.user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return this.user.getEmail();
        }

        /*
         ACTIVE only. PENDING_VERIFICATION deliberately fails here: an account
         is unusable until the emailed code has proved the student owns the
         mailbox, which is the whole point of the verification gate.

         Spring turns this into a DisabledException, which GlobalExceptionHandler
         maps to 403 EMAIL_NOT_VERIFIED so the client can route to the code screen.
        */
        @Override
        public boolean isEnabled() {
            return this.user.getAccountStatus() == AccountStatus.ACTIVE;
        }

        @Override
        public boolean isAccountNonLocked() {
            return this.user.getAccountStatus() != AccountStatus.SUSPENDED;
        }

    }

}
