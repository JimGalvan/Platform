package com.platform.common.exception;

import com.platform.common.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        return Response.status(422)
            .entity(new ErrorResponse("INVALID_ARGUMENT", exception.getMessage()))
            .build();
    }
}