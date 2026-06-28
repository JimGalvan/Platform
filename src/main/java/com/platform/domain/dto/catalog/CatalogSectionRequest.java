package com.platform.domain.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogSectionRequest(
    @NotBlank
    @Size(max = 120)
    String name
) {
}