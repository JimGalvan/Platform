package com.platform.common.exception;

import com.platform.common.ErrorResponse;

import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SecurityExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Override
    public Response toResponse(UnauthorizedException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
            .entity(new ErrorResponse(
                "UNAUTHORIZED",
                "Missing or invalid authentication token."
            ))
            .build();
    }
}
