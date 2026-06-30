package com.platform.catalog.domain.keys;

import java.util.Optional;

/**
 * A catalog-scoped image slot. Each kind owns the {@link CatalogProperty} key it
 * writes and the storage folder its object lives in, so a single upload endpoint
 * can serve every slot. The URL slug is the lower-case folder name (e.g. {@code logo}).
 */
public enum CatalogMediaKind {

    LOGO(CatalogPropertyKey.LOGO, "logo"),
    COVER(CatalogPropertyKey.COVER, "cover");

    private final String propertyKey;
    private final String folder;

    CatalogMediaKind(String propertyKey, String folder) {
        this.propertyKey = propertyKey;
        this.folder = folder;
    }

    /** The {@code CatalogProperty} name this slot is stored under. */
    public String propertyKey() {
        return propertyKey;
    }

    /** The {@code catalogs/{id}/{folder}/} storage prefix for this slot. */
    public String folder() {
        return folder;
    }

    /** Resolves a URL slug (case-insensitive) to its kind, empty when unknown. */
    public static Optional<CatalogMediaKind> fromSlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        for (CatalogMediaKind kind : values()) {
            if (kind.folder.equalsIgnoreCase(slug)) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }
}
