package com.platform.catalog.common.exception;

import com.platform.catalog.domain.enums.CatalogError;

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
