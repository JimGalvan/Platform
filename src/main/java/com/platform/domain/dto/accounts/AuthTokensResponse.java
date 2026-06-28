package com.platform.domain.dto.accounts;

import com.platform.domain.model.AuthTokens;

public record AuthTokensResponse(String token, String refreshToken) {

    public static AuthTokensResponse from(AuthTokens tokens) {
        return new AuthTokensResponse(tokens.token(), tokens.refreshToken());
    }
}
