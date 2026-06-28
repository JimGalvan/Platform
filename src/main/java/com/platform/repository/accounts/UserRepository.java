package com.platform.repository.accounts;

import com.platform.domain.entities.accounts.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {

    public Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<UserEntity> findByRefreshTokenHash(String refreshTokenHash) {
        return find("refreshTokenHash", refreshTokenHash).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}
