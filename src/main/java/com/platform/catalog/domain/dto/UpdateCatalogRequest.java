package com.platform.catalog.domain.dto;

import com.platform.catalog.domain.entities.CatalogProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCatalogRequest(
    @NotBlank @Size(max = 120) String name,
    List<@Valid CatalogProperty> properties
) {
}