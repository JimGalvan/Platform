package com.platform.catalog.domain.keys;

/**
 * Well-known {@code name} keys for {@link com.platform.catalog.domain.entities.CatalogProperty}
 * entries. Media keys hold an object-storage key whose outbound value is presigned
 * (see {@link com.platform.catalog.domain.dto.CatalogPropertyResponse}).
 */
public final class CatalogPropertyKey {

    /** Catalog logo image (square), shown as the avatar. */
    public static final String LOGO = "logoObjectKey";

    /** Catalog cover/header image (wide banner), shown at the top of the menu. */
    public static final String COVER = "coverObjectKey";

    private CatalogPropertyKey() {
    }
}
