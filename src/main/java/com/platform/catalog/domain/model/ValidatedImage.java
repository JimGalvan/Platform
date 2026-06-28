package com.platform.catalog.domain.model;

public record ValidatedImage(
    byte[] bytes,
    String contentType,
    String extension,
    int width,
    int height
) {
}
