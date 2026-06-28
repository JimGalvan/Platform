package com.platform.domain.dto.catalog;

import com.platform.common.storage.ObjectStorage;
import com.platform.domain.entities.catalog.CatalogSectionEntity;
import com.platform.domain.entities.catalog.CatalogEntity;
import com.platform.domain.entities.catalog.CatalogItemEntity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record PublicCatalogResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String market,
    String currency,
    String phone,
    String address,
    String operatingHours,
    String email,
    String logoUrl,
    List<PublicCatalogSectionResponse> sections,
    List<PublicCatalogItemResponse> items,
    List<PublicCatalogItemResponse> uncategorizedItems
) {

    public static PublicCatalogResponse from(
        CatalogEntity catalog,
        ObjectStorage objectStorage,
        String contactEmail
    ) {
        List<CatalogItemEntity> visibleItems = catalog.getItems().stream()
            .filter(CatalogItemEntity::isVisible)
            .sorted(Comparator
                .comparing((CatalogItemEntity item) -> sectionId(item) == null
                    ? Integer.MAX_VALUE
                    : sectionPosition(catalog, item))
                .thenComparingInt(CatalogItemEntity::getPosition))
            .toList();
        return new PublicCatalogResponse(
            catalog.getId(),
            catalog.getName(),
            catalog.getSlug(),
            catalog.getDescription(),
            catalog.getMarket(),
            catalog.getCurrency(),
            catalog.getPhone(),
            catalog.getAddress(),
            catalog.getOperatingHours(),
            contactEmail,
            catalog.getLogoObjectKey() == null
                ? null
                : objectStorage.presignedReadUrl(catalog.getLogoObjectKey()),
            catalog.getSections().stream()
                .sorted(Comparator.comparingInt(CatalogSectionEntity::getPosition))
                .map(PublicCatalogSectionResponse::from)
                .toList(),
            visibleItems.stream()
                .filter(item -> sectionId(item) != null)
                .map(item -> PublicCatalogItemResponse.from(item, objectStorage))
                .toList(),
            visibleItems.stream()
                .filter(item -> sectionId(item) == null)
                .map(item -> PublicCatalogItemResponse.from(item, objectStorage))
                .toList()
        );
    }

    private static UUID sectionId(CatalogItemEntity item) {
        return item.getSection() == null ? null : item.getSection().getId();
    }

    private static int sectionPosition(CatalogEntity catalog, CatalogItemEntity item) {
        return catalog.getSections().stream()
            .filter(section -> section.getId().equals(sectionId(item)))
            .findFirst()
            .map(CatalogSectionEntity::getPosition)
            .orElse(Integer.MAX_VALUE);
    }
}
