package com.platform.catalog.core.creation;

import com.platform.catalog.common.exception.CatalogException;
import com.platform.catalog.domain.enums.CatalogError;
import com.platform.catalog.repository.CatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class UniqueCatalogSlugGenerator {

    private static final int MAX_LENGTH = 120;
    private static final int MAXIMUM_ATTEMPTS = 10;

    private final CatalogRepository catalogRepository;

    public UniqueCatalogSlugGenerator(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public String generate(String catalogName) {
        String baseSlug = slugFromName(catalogName);
        if (!catalogRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }
        for (int attempt = 0; attempt < MAXIMUM_ATTEMPTS; attempt++) {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            String candidate = withSuffix(baseSlug, suffix);
            if (!catalogRepository.existsBySlug(candidate)) {
                return candidate;
            }
        }
        throw new CatalogException(
            CatalogError.CATALOG_SLUG_CONFLICT,
            "A unique catalog URL could not be generated."
        );
    }

    private static String slugFromName(String name) {
        String normalizedName = Normalizer.normalize(requireName(name), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
        if (normalizedName.isBlank()) {
            normalizedName = "catalog";
        }
        if (normalizedName.length() > MAX_LENGTH) {
            normalizedName = normalizedName.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return normalizedName;
    }

    private static String withSuffix(String base, String suffix) {
        String normalizedSuffix = Objects.requireNonNull(suffix, "Slug suffix is required.")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]", "");
        if (normalizedSuffix.isBlank()) {
            throw new IllegalArgumentException("Slug suffix is invalid.");
        }
        int baseLength = MAX_LENGTH - normalizedSuffix.length() - 1;
        String trimmedBase = base.substring(0, Math.min(base.length(), baseLength))
            .replaceAll("-+$", "");
        return trimmedBase + "-" + normalizedSuffix;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Catalog name is required.");
        }
        return name.trim();
    }
}
