package com.platform.catalog.domain.dto;

import com.platform.common.storage.ObjectStorage;
import com.platform.catalog.domain.entities.CatalogItemEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CatalogItemResponse(
    UUID id,
    UUID sectionId,
    String name,
    String description,
    BigDecimal priceAmount,
    boolean visible,
    boolean soldOut,
    int position,
    String imageUrl,
    Instant createdAt,
    Instant updatedAt
) {

    public static CatalogItemResponse from(
        CatalogItemEntity item,
        ObjectStorage objectStorage
    ) {
        return new CatalogItemResponse(
            item.getId(),
            item.getSection() == null ? null : item.getSection().getId(),
            item.getName(),
            item.getDescription(),
            item.getPriceAmount(),
            item.isVisible(),
            item.isSoldOut(),
            item.getPosition(),
            item.getImageObjectKey() == null
                ? null
                : objectStorage.presignedReadUrl(item.getImageObjectKey()),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
