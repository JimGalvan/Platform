package com.platform.catalog.domain.dto;

import com.platform.common.storage.ObjectStorage;
import com.platform.catalog.domain.keys.CatalogPropertyKey;
import com.platform.catalog.domain.entities.CatalogSectionEntity;
import com.platform.catalog.domain.entities.CatalogEntity;
import com.platform.catalog.domain.entities.CatalogItemEntity;
import com.platform.catalog.domain.entities.CatalogProperty;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record PublicCatalogResponse(
    UUID id,
    String name,
    String slug,
    String logoUrl,
    String coverUrl,
    List<CatalogProperty> properties,
    List<PublicCatalogSectionResponse> sections,
    List<PublicCatalogItemResponse> items,
    List<PublicCatalogItemResponse> uncategorizedItems
) {

    public static PublicCatalogResponse from(
        CatalogEntity catalog,
        ObjectStorage objectStorage
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
            presignedProperty(catalog, CatalogPropertyKey.LOGO, objectStorage),
            presignedProperty(catalog, CatalogPropertyKey.COVER, objectStorage),
            catalog.getProperties(),
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

    private static String presignedProperty(CatalogEntity catalog, String key, ObjectStorage objectStorage) {
        return catalog.property(key)
            .map(CatalogProperty::getValue)
            .map(objectStorage::presignedReadUrl)
            .orElse(null);
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