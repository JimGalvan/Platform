package com.platform.catalog.domain.dto;

import com.platform.common.storage.ObjectStorage;
import com.platform.catalog.domain.entities.CatalogEntity;
import com.platform.catalog.domain.entities.CatalogProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CatalogSummaryResponse(
    UUID id,
    String name,
    String slug,
    String logoUrl,
    List<CatalogProperty> properties,
    Instant updatedAt
) {

    public static CatalogSummaryResponse from(CatalogEntity catalog, ObjectStorage objectStorage) {
        return new CatalogSummaryResponse(
            catalog.getId(),
            catalog.getName(),
            catalog.getSlug(),
            catalog.property("logoObjectKey")
                .map(CatalogProperty::getValue)
                .map(objectStorage::presignedReadUrl)
                .orElse(null),
            catalog.getProperties(),
            catalog.getUpdatedAt()
        );
    }
}