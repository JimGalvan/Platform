package com.platform.accounts.controllers;

import com.platform.common.Result;
import com.platform.common.controller.BaseController;
import com.platform.accounts.core.AccountCore;
import com.platform.accounts.domain.dto.AccountResponse;
import com.platform.accounts.domain.dto.ChangePasswordRequest;
import com.platform.accounts.domain.dto.CreateAccountRequest;
import com.platform.accounts.domain.entities.UserEntity;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;


@Path("/api/v1/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class AccountController extends BaseController {

    private final AccountCore accountCore;
    private final JsonWebToken jsonWebToken;

    public AccountController(
        AccountCore accountCore,
        JsonWebToken jsonWebToken
    ) {
        this.accountCore = accountCore;
        this.jsonWebToken = jsonWebToken;
    }

    @POST
    public Response register(@Valid CreateAccountRequest request) {
        Result<UserEntity> result = accountCore.register(request.email(), request.password());
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.created(AccountResponse.from(result.getValue())));
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response getCurrentAccount() {
        Result<UserEntity> result = accountCore.getCurrent(getAuthenticatedUserId(jsonWebToken));
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(AccountResponse.from(result.getValue())));
    }

    @PUT
    @Path("/password")
    @Authenticated
    public Response changePassword(@Valid ChangePasswordRequest request) {
        return toResponse(accountCore.changePassword(
            getAuthenticatedUserId(jsonWebToken),
            request.currentPassword(),
            request.newPassword()
        ));
    }
}
