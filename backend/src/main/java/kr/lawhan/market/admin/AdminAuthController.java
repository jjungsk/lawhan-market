package kr.lawhan.market.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.lawhan.market.admin.dto.AdminLoginRequest;
import kr.lawhan.market.admin.dto.AdminMeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Logout is handled declaratively by Spring Security's {@code logout()} DSL in
 * SecurityConfig, not here — see that class for the /api/admin/logout wiring.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final LoginAttemptGuard loginAttemptGuard;

    public AdminAuthController(AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            LoginAttemptGuard loginAttemptGuard) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.loginAttemptGuard = loginAttemptGuard;
    }

    @PostMapping("/login")
    public AdminMeResponse login(@Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        loginAttemptGuard.checkNotLocked(request.email());

        Authentication authResult;
        try {
            authResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            loginAttemptGuard.onFailure(request.email());
            // Same message regardless of whether the account exists — avoids leaking which admin emails are valid.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid email or password");
        }
        loginAttemptGuard.onSuccess(request.email());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return new AdminMeResponse(authResult.getName());
    }

    @GetMapping("/me")
    public AdminMeResponse me(Authentication authentication) {
        return new AdminMeResponse(authentication.getName());
    }
}
