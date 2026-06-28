package com.platform.domain.dto.catalog;

import com.platform.domain.entities.catalog.CatalogSectionEntity;

import java.util.UUID;

public record PublicCatalogSectionResponse(
    UUID id,
    String name,
    int position
) {

    public static PublicCatalogSectionResponse from(CatalogSectionEntity section) {
        return new PublicCatalogSectionResponse(
            section.getId(),
            section.getName(),
            section.getPosition()
        );
    }
}
