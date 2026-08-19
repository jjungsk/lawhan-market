package kr.lawhan.market.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * M4: session-based auth for /admin/**, public read/write APIs unchanged.
 *
 * <p><b>CSRF:</b> disabled. Session cookie CSRF is normally needed for browser clients,
 * but two things make it unnecessary here: (1) the frontend/backend are served from the
 * same origin behind Nginx (docs/architecture-requirements.md §3) with no CORS allow-list,
 * so a cross-site page cannot get a browser to complete a state-changing fetch against
 * this API in the first place; (2) the session cookie is issued with {@code SameSite=Strict}
 * (see application.yml), so it is never attached to a cross-site request at all — the
 * cookie itself is the CSRF defense. Adding token-based CSRF on top would mean threading
 * a token through every Next.js fetch call for no real reduction in risk at this app's
 * scale (single admin, internal tool). Revisit if a legitimate cross-origin client (e.g. a
 * separate admin domain) is introduced later.
 *
 * <p><b>Session cookie:</b> {@code HttpSessionSecurityContextRepository}, created lazily
 * (IF_REQUIRED) only when a login actually succeeds — public GET traffic never causes a
 * session/cookie to be issued. {@code Secure} is left to Spring Boot's per-request default
 * (mirrors whether the incoming request was HTTPS) rather than hardcoded true, since local
 * dev and the initial no-SSL EC2 deployment stage (§3) are plain HTTP; once the real domain
 * + SSL is live (M11), confirm Nginx forwards {@code X-Forwarded-Proto} so the cookie is
 * correctly marked Secure in prod.
 *
 * <p><b>Brute force:</b> {@link kr.lawhan.market.admin.LoginAttemptGuard} does a minimal
 * in-memory per-email lockout after repeated failures — see that class for why nothing
 * heavier is warranted here.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Boot's error dispatch (BasicErrorController) is an internal forward to
                        // GET/error for ANY endpoint's exception response (400/404/...), including
                        // public ones — without this, an anonymous request that fails validation on
                        // a permitAll endpoint gets its real status masked by a 401 from this filter.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/listings/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/admin/logout"))
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                );
        return http.build();
    }
}
