package com.platform.catalog.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderCatalogItemsRequest(
    UUID sectionId,
    @NotNull List<UUID> itemIds
) {
}
