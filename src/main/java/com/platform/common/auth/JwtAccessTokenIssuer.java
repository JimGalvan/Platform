package com.platform.common.auth;

import com.platform.domain.entities.accounts.UserEntity;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class JwtAccessTokenIssuer {

    private final Duration accessTokenTimeToLive;

    public JwtAccessTokenIssuer(
        @ConfigProperty(name = "accounts.jwt.time-to-live")
        long accessTokenTimeToLiveSeconds
    ) {
        this.accessTokenTimeToLive = Duration.ofSeconds(accessTokenTimeToLiveSeconds);
    }

    public String issueFor(UserEntity user) {
        return Jwt.subject(user.getId().toString())
            .claim("email", user.getEmail())
            .groups(Set.of("user"))
            .expiresIn(accessTokenTimeToLive)
            .sign();
    }
}
