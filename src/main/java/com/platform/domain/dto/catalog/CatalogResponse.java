package com.platform.domain.dto.catalog;

import com.platform.common.storage.ObjectStorage;
import com.platform.domain.entities.catalog.CatalogEntity;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CatalogResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String market,
    String currency,
    String phone,
    String address,
    String operatingHours,
    boolean showEmail,
    String logoUrl,
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
            catalog.getDescription(),
            catalog.getMarket(),
            catalog.getCurrency(),
            catalog.getPhone(),
            catalog.getAddress(),
            catalog.getOperatingHours(),
            catalog.isShowEmail(),
            mediaUrl(catalog.getLogoObjectKey(), objectStorage),
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
