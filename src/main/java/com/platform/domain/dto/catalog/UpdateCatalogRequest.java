package com.platform.domain.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCatalogRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 1000) String description,
    @Size(max = 50) String phone,
    @Size(max = 500) String address,
    @Size(max = 1000) String operatingHours,
    boolean showEmail
) {
}
