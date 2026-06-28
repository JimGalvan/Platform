package com.platform.domain.dto.catalog;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderCatalogItemsRequest(
    UUID sectionId,
    @NotNull List<UUID> itemIds
) {
}
