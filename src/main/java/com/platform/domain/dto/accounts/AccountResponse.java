package com.platform.domain.dto.accounts;

import com.platform.domain.entities.accounts.UserEntity;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String email,
    Instant createdAt,
    Instant updatedAt
) {
    public static AccountResponse from(UserEntity user) {
        return new AccountResponse(
            user.getId(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
