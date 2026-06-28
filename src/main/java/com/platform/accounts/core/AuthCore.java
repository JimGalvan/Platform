package com.platform.accounts.core;

import com.platform.common.Result;
import com.platform.accounts.common.auth.JwtAccessTokenIssuer;
import com.platform.accounts.common.auth.PasswordHasher;
import com.platform.accounts.common.auth.RefreshTokenManager;
import com.platform.accounts.domain.entities.UserEntity;
import com.platform.accounts.domain.model.AuthTokens;
import com.platform.accounts.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class AuthCore {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtAccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenManager refreshTokenManager;
    private final Duration refreshTokenTimeToLive;

    public AuthCore(
        UserRepository userRepository,
        PasswordHasher passwordHasher,
        JwtAccessTokenIssuer accessTokenIssuer,
        RefreshTokenManager refreshTokenManager,
        @ConfigProperty(name = "accounts.refresh-token.time-to-live")
        long refreshTokenTimeToLiveSeconds
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenManager = refreshTokenManager;
        this.refreshTokenTimeToLive = Duration.ofSeconds(refreshTokenTimeToLiveSeconds);
    }

    @Transactional
    public Result<AuthTokens> login(String email, String password) {
        UserEntity user = userRepository.findByEmail(AccountCore.normalizeEmail(email))
            .orElse(null);

        if (user == null || !passwordHasher.verify(password, user.getPasswordHash())) {
            return Result.unauthorized("INVALID_CREDENTIALS", "Invalid email or password.");
        }

        Instant currentTime = Instant.now();
        if (passwordHasher.needsRehash(user.getPasswordHash())) {
            user.setPasswordHash(passwordHasher.hash(password));
        }

        String refreshToken = issueRefreshToken(user, currentTime);
        return Result.ok(new AuthTokens(accessTokenIssuer.issueFor(user), refreshToken));
    }

    @Transactional
    public Result<AuthTokens> refresh(String refreshToken) {
        String currentTokenHash = refreshTokenManager.hash(refreshToken);
        UserEntity user = userRepository.findByRefreshTokenHash(currentTokenHash).orElse(null);
        Instant currentTime = Instant.now();

        if (user == null || !refreshTokenIsActiveAt(user, currentTime)) {
            return Result.unauthorized(
                "INVALID_REFRESH_TOKEN",
                "Invalid or expired refresh token."
            );
        }

        String rotatedRefreshToken = issueRefreshToken(user, currentTime);
        return Result.ok(new AuthTokens(accessTokenIssuer.issueFor(user), rotatedRefreshToken));
    }

    @Transactional
    public Result<Void> logout(UUID userId) {
        Result<UserEntity> userResult = AccountCore.requireUser(userRepository, userId);
        if (!userResult.isSuccess()) {
            return userResult.asError();
        }
        AccountCore.revokeRefreshToken(userResult.getValue(), Instant.now());
        return Result.noContent();
    }

    private String issueRefreshToken(UserEntity user, Instant currentTime) {
        String refreshToken = refreshTokenManager.generate();
        user.setRefreshTokenHash(refreshTokenManager.hash(refreshToken));
        user.setRefreshTokenExpiry(currentTime.plus(refreshTokenTimeToLive));
        user.setUpdatedAt(currentTime);
        return refreshToken;
    }

    private static boolean refreshTokenIsActiveAt(UserEntity user, Instant currentTime) {
        return user.getRefreshTokenHash() != null
            && user.getRefreshTokenExpiry() != null
            && currentTime.isBefore(user.getRefreshTokenExpiry());
    }
}
