package com.platform.catalog.domain.dto;

import com.platform.common.storage.ObjectStorage;
import com.platform.catalog.domain.entities.CatalogEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CatalogSummaryResponse(
    UUID id,
    String name,
    String slug,
    List<CatalogPropertyResponse> properties,
    Instant updatedAt
) {

    public static CatalogSummaryResponse from(CatalogEntity catalog, ObjectStorage objectStorage) {
        return new CatalogSummaryResponse(
            catalog.getId(),
            catalog.getName(),
            catalog.getSlug(),
            catalog.getProperties().stream()
                .map(property -> CatalogPropertyResponse.from(property, objectStorage))
                .toList(),
            catalog.getUpdatedAt()
        );
    }
}