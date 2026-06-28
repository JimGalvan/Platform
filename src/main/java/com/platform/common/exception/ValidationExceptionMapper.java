package com.platform.common.exception;

import com.platform.common.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
            .findFirst()
            .map(violation -> {
                String propertyPath = violation.getPropertyPath().toString();
                String fieldName = propertyPath.contains(".")
                    ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                    : propertyPath;
                return fieldName + ": " + violation.getMessage();
            })
            .orElse("Validation failed.");
        return Response.status(422)
            .entity(new ErrorResponse("VALIDATION_FAILED", message))
            .build();
    }
}
