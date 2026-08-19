package kr.lawhan.market.admin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal brute-force guard: locks a single email out for {@link #LOCK_DURATION} after
 * {@link #MAX_ATTEMPTS} consecutive failures. In-memory and per-instance — acceptable
 * because this app runs as a single instance (docs/architecture-requirements.md §3) and
 * this is only meant to blunt naive automated guessing, not a full account-security
 * subsystem (no captcha/IP tracking/persistence — out of scope for this app's size).
 */
@Component
public class LoginAttemptGuard {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();

    public void checkNotLocked(String email) {
        Attempts attempts = attemptsByEmail.get(normalize(email));
        if (attempts != null && attempts.isLocked()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "too many failed login attempts, try again later");
        }
    }

    public void onFailure(String email) {
        attemptsByEmail.computeIfAbsent(normalize(email), k -> new Attempts()).recordFailure();
    }

    public void onSuccess(String email) {
        attemptsByEmail.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static final class Attempts {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile Instant lockedUntil = Instant.EPOCH;

        void recordFailure() {
            if (count.incrementAndGet() >= MAX_ATTEMPTS) {
                lockedUntil = Instant.now().plus(LOCK_DURATION);
            }
        }

        boolean isLocked() {
            return Instant.now().isBefore(lockedUntil);
        }
    }
}
