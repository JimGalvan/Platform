package com.platform.catalog.domain.dto;

import com.platform.common.storage.ObjectStorage;
import com.platform.catalog.domain.entities.CatalogEntity;
import com.platform.catalog.domain.entities.CatalogProperty;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CatalogResponse(
    UUID id,
    String name,
    String slug,
    String logoUrl,
    List<CatalogProperty> properties,
    List<CatalogSectionResponse> sections,
    List<CatalogItemResponse> items,
    Instant createdAt,
    Instant updatedAt
) {

    public static CatalogResponse from(CatalogEntity catalog, ObjectStorage objectStorage) {

        return new CatalogResponse(
            catalog.getId(),
            catalog.getName(),
            catalog.getSlug(),
            mediaUrl(catalog.property("logoObjectKey").map(CatalogProperty::getValue).orElse(null), objectStorage),
            catalog.getProperties(),
            catalog.getSections().stream()
                .sorted(Comparator.comparingInt(section -> section.getPosition()))
                .map(CatalogSectionResponse::from)
                .toList(),
            catalog.getItems().stream()
                .map(item -> CatalogItemResponse.from(item, objectStorage))
                .toList(),
            catalog.getCreatedAt(),
            catalog.getUpdatedAt()
        );
    }

    private static String mediaUrl(String objectKey, ObjectStorage objectStorage) {
        return objectKey == null ? null : objectStorage.presignedReadUrl(objectKey);
    }
}