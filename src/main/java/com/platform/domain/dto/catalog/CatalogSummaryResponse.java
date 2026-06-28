package com.platform.domain.dto.catalog;

import com.platform.common.storage.ObjectStorage;
import com.platform.domain.entities.catalog.CatalogEntity;

import java.time.Instant;
import java.util.UUID;

public record CatalogSummaryResponse(
    UUID id,
    String name,
    String slug,
    String market,
    String currency,
    String logoUrl,
    Instant updatedAt
) {

    public static CatalogSummaryResponse from(CatalogEntity catalog, ObjectStorage objectStorage) {
        return new CatalogSummaryResponse(
            catalog.getId(),
            catalog.getName(),
            catalog.getSlug(),
            catalog.getMarket(),
            catalog.getCurrency(),
            catalog.getLogoObjectKey() == null
                ? null
                : objectStorage.presignedReadUrl(catalog.getLogoObjectKey()),
            catalog.getUpdatedAt()
        );
    }
}
