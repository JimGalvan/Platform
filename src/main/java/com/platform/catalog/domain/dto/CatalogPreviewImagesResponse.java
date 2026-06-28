package com.platform.catalog.domain.dto;

import com.platform.common.storage.ObjectStorage;

import java.util.List;

public record CatalogPreviewImagesResponse(
    List<String> previewImageUrls
) {

    public static CatalogPreviewImagesResponse from(
        List<String> imageObjectKeys,
        ObjectStorage objectStorage
    ) {
        return new CatalogPreviewImagesResponse(
            imageObjectKeys.stream()
                .map(objectStorage::presignedReadUrl)
                .toList()
        );
    }
}
