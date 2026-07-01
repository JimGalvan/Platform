package com.platform.catalog.domain.dto;

import com.platform.catalog.domain.entities.CatalogProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCatalogRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 1000) String description,
    List<@Valid CatalogProperty> properties
) {
}