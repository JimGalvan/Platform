package com.platform.common.exception;

import com.platform.domain.enums.catalog.CatalogError;

public final class CatalogException extends RuntimeException {

    private final CatalogError error;

    public CatalogException(CatalogError error, String message) {
        super(message);
        this.error = error;
    }

    public CatalogError error() {
        return error;
    }
}
