package com.platform.domain.dto.accounts;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
    @NotBlank @Email @Size(max = 254)
    String email,
    @NotBlank @Size(min = 8, max = 64)
    String password
) {
}
