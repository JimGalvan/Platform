package com.platform.catalog.common.exception;

import com.platform.common.ErrorResponse;

import com.platform.catalog.common.exception.CatalogException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CatalogExceptionMapper implements ExceptionMapper<CatalogException> {

    @Override
    public Response toResponse(CatalogException exception) {
        int status = switch (exception.error()) {
            case CATALOG_NOT_FOUND, SECTION_NOT_FOUND, ITEM_NOT_FOUND ->
                Response.Status.NOT_FOUND.getStatusCode();
            case INVALID_MARKET, INVALID_SECTION_ORDER, INVALID_ITEM_SECTION,
                INVALID_ITEM_ORDER, INVALID_IMAGE -> 422;
            case CATALOG_SLUG_CONFLICT -> Response.Status.CONFLICT.getStatusCode();
        };
        return Response.status(status)
            .entity(new ErrorResponse(exception.error().name(), exception.getMessage()))
            .build();
    }
}
