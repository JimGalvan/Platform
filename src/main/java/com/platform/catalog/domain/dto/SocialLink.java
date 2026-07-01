package com.platform.catalog.domain.dto;

/**
 * One creator social link. {@code key} identifies the platform (e.g. {@code instagram}),
 * {@code handle} is the raw username/path, and {@code visible} toggles it on the public
 * "Find me" sheet. Persisted as a JSON array in the {@code socialLinks} catalog property.
 */
public record SocialLink(
    String key,
    String handle,
    boolean visible
) {
}
