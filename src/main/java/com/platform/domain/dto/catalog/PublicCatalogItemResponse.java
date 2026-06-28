package com.platform.domain.dto.catalog;

import com.platform.common.storage.ObjectStorage;
import com.platform.domain.entities.catalog.CatalogItemEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicCatalogItemResponse(
    UUID id,
    UUID sectionId,
    String name,
    String description,
    BigDecimal priceAmount,
    boolean soldOut,
    int position,
    String imageUrl
) {

    public static PublicCatalogItemResponse from(
        CatalogItemEntity item,
        ObjectStorage objectStorage
    ) {
        return new PublicCatalogItemResponse(
            item.getId(),
            item.getSection() == null ? null : item.getSection().getId(),
            item.getName(),
            item.getDescription(),
            item.getPriceAmount(),
            item.isSoldOut(),
            item.getPosition(),
            item.getImageObjectKey() == null
                ? null
                : objectStorage.presignedReadUrl(item.getImageObjectKey())
        );
    }
}
