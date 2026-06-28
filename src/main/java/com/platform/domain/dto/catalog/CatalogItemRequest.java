package com.platform.domain.dto.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogItemRequest(
    UUID sectionId,
    @NotBlank @Size(max = 160) String name,
    @Size(max = 1000) String description,
    @NotNull @DecimalMin("0.00") BigDecimal priceAmount,
    Boolean visible,
    Boolean soldOut
) {

    public boolean visibleOrDefault() {
        return visible == null || visible;
    }

    public boolean soldOutOrDefault() {
        return soldOut != null && soldOut;
    }
}
