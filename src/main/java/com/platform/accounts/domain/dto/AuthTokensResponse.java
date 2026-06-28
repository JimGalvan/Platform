package com.platform.accounts.domain.dto;

import com.platform.accounts.domain.model.AuthTokens;

public record AuthTokensResponse(String token, String refreshToken) {

    public static AuthTokensResponse from(AuthTokens tokens) {
        return new AuthTokensResponse(tokens.token(), tokens.refreshToken());
    }
}
