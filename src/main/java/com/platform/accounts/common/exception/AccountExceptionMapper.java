package com.platform.accounts.common.exception;

import com.platform.common.ErrorResponse;
import com.platform.accounts.common.exception.AccountException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AccountExceptionMapper implements ExceptionMapper<AccountException> {

    @Override
    public Response toResponse(AccountException exception) {
        int status = switch (exception.error()) {
            case EMAIL_ALREADY_REGISTERED -> Response.Status.CONFLICT.getStatusCode();
            case INVALID_CREDENTIALS, INVALID_REFRESH_TOKEN ->
                Response.Status.UNAUTHORIZED.getStatusCode();
            case USER_NOT_FOUND -> Response.Status.NOT_FOUND.getStatusCode();
        };
        return Response.status(status)
            .entity(new ErrorResponse(exception.error().name(), exception.getMessage()))
            .build();
    }
}
