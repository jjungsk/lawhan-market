package kr.lawhan.market.admin;

import kr.lawhan.market.user.User;
import kr.lawhan.market.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Backs Spring Security's authentication with the {@code users} table. Loads by email
 * regardless of role and grants {@code ROLE_<role>} — SecurityConfig's authorization
 * rules are what actually restrict /api/admin/** to ROLE_ADMIN, so this stays reusable
 * if/when a 'member' role is introduced.
 */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AdminUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("no such user: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().toUpperCase())
                .build();
    }
}
