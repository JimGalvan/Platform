package com.platform.accounts.controllers;

import com.platform.common.Result;
import com.platform.common.controller.BaseController;
import com.platform.accounts.core.AuthCore;
import com.platform.accounts.domain.dto.AuthTokensResponse;
import com.platform.accounts.domain.dto.LoginRequest;
import com.platform.accounts.domain.dto.RefreshRequest;
import com.platform.accounts.domain.model.AuthTokens;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;


@Path("/api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class AuthController extends BaseController {

    private final AuthCore authCore;
    private final JsonWebToken jsonWebToken;

    public AuthController(
        AuthCore authCore,
        JsonWebToken jsonWebToken
    ) {
        this.authCore = authCore;
        this.jsonWebToken = jsonWebToken;
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        Result<AuthTokens> result = authCore.login(request.email(), request.password());
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(AuthTokensResponse.from(result.getValue())));
    }

    @POST
    @Path("/refresh")
    public Response refresh(@Valid RefreshRequest request) {
        Result<AuthTokens> result = authCore.refresh(request.refreshToken());
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(AuthTokensResponse.from(result.getValue())));
    }

    @POST
    @Path("/logout")
    @Authenticated
    @Consumes(MediaType.WILDCARD)
    public Response logout() {
        return toResponse(authCore.logout(getAuthenticatedUserId(jsonWebToken)));
    }
}
