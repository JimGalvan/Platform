package com.platform.catalog.domain.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderCatalogSectionsRequest(
    @NotEmpty
    List<UUID> sectionIds
) {
}