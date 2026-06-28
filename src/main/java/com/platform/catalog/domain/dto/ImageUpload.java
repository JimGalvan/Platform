package com.platform.catalog.domain.dto;

public record ImageUpload(
    byte[] bytes,
    String contentType,
    String originalFileName
) {
}
