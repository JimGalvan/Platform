package com.platform.common.auth;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@ApplicationScoped
public class RefreshTokenManager {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public String hash(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
