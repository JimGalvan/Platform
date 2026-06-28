package com.platform.catalog.domain.dto;

import com.platform.catalog.domain.entities.CatalogSectionEntity;

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
