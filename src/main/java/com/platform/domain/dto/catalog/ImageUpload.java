package com.platform.domain.dto.catalog;

public record ImageUpload(
    byte[] bytes,
    String contentType,
    String originalFileName
) {
}
