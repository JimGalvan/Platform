package com.platform.controller;

import com.platform.common.ErrorResponse;
import com.platform.common.Result;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

public abstract class BaseController {

    protected <T> Response toResponse(Result<T> result) {
        if (result.isSuccess()) {
            if (result.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                return Response.noContent().build();
            }
            return Response.status(result.getStatus()).entity(result.getValue()).build();
        }
        return Response.status(result.getStatus())
            .entity(new ErrorResponse(result.getCode(), result.getMessage()))
            .build();
    }

    protected UUID getAuthenticatedUserId(JsonWebToken jsonWebToken) {
        return UUID.fromString(jsonWebToken.getSubject());
    }
}
