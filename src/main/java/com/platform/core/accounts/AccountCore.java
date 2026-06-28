package com.platform.core.accounts;

import com.platform.common.Result;
import com.platform.common.auth.PasswordHasher;
import com.platform.domain.entities.accounts.UserEntity;
import com.platform.repository.accounts.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class AccountCore {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AccountCore(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public Result<UserEntity> register(String emailValue, String password) {
        String email = normalizeEmail(emailValue);
        if (userRepository.existsByEmail(email)) {
            return Result.conflict(
                "EMAIL_ALREADY_REGISTERED",
                "A user with that email already exists."
            );
        }

        Instant now = Instant.now();
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordHasher.hash(password));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.persist(user);
        return Result.created(user);
    }

    public Result<UserEntity> getCurrent(UUID userId) {
        return requireUser(userRepository, userId);
    }

    @Transactional
    public Result<Void> changePassword(
        UUID userId,
        String currentPassword,
        String newPassword
    ) {
        Result<UserEntity> userResult = requireUser(userRepository, userId);
        if (!userResult.isSuccess()) {
            return userResult.asError();
        }
        UserEntity user = userResult.getValue();
        if (!passwordHasher.verify(currentPassword, user.getPasswordHash())) {
            return Result.unauthorized(
                "INVALID_CREDENTIALS",
                "Current password is incorrect."
            );
        }

        user.setPasswordHash(passwordHasher.hash(newPassword));
        revokeRefreshToken(user, Instant.now());
        return Result.noContent();
    }

    @Transactional
    public void resetPasswordManually(String emailValue, String newPassword) {
        UserEntity user = userRepository.findByEmail(normalizeEmail(emailValue))
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setPasswordHash(passwordHasher.hash(newPassword));
        revokeRefreshToken(user, Instant.now());
    }

    static Result<UserEntity> requireUser(UserRepository userRepository, UUID userId) {
        return userRepository.findByIdOptional(userId)
            .map(Result::ok)
            .orElseGet(() -> Result.notFound("USER_NOT_FOUND", "User not found."));
    }

    static void revokeRefreshToken(UserEntity user, Instant revokedAt) {
        user.setRefreshTokenHash(null);
        user.setRefreshTokenExpiry(null);
        user.setUpdatedAt(revokedAt);
    }

    static String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
