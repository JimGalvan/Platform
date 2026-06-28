package com.platform.catalog.domain.dto;

import com.platform.catalog.domain.entities.CatalogSectionEntity;

import java.util.UUID;

public record CatalogSectionResponse(
    UUID id,
    String name,
    int position
) {

    public static CatalogSectionResponse from(CatalogSectionEntity section) {
        return new CatalogSectionResponse(
            section.getId(),
            section.getName(),
            section.getPosition()
        );
    }
}
