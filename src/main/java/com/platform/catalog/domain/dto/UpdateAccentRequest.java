package com.platform.catalog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateAccentRequest(
    @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String accentColor
) {
}
