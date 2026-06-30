package com.platform.catalog.domain.dto;

import com.platform.common.storage.ObjectStorage;
import com.platform.catalog.domain.entities.CatalogProperty;
import com.platform.catalog.domain.enums.CatalogPropertyType;

/**
 * Outbound view of a {@link CatalogProperty}. {@code MEDIA} properties store an
 * object key in the database; the {@code value} exposed here is a presigned read
 * URL so clients can render the media directly (e.g. the catalog logo lives in
 * the {@code logoObjectKey} property).
 */
public record CatalogPropertyResponse(
    String name,
    CatalogPropertyType type,
    String value
) {

    public static CatalogPropertyResponse from(CatalogProperty property, ObjectStorage objectStorage) {
        String value = property.getType() == CatalogPropertyType.MEDIA
            ? objectStorage.presignedReadUrl(property.getValue())
            : property.getValue();
        return new CatalogPropertyResponse(property.getName(), property.getType(), value);
    }
}
