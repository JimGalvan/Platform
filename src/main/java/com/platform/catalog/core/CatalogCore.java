package com.platform.catalog.core;

import com.platform.common.Result;
import com.platform.catalog.common.exception.CatalogException;
import com.platform.catalog.core.creation.UniqueCatalogSlugGenerator;
import com.platform.catalog.domain.dto.CreateCatalogRequest;
import com.platform.catalog.domain.dto.UpdateCatalogRequest;
import com.platform.catalog.domain.entities.CatalogEntity;
import com.platform.catalog.domain.entities.CatalogItemEntity;
import com.platform.catalog.domain.entities.CatalogProperty;
import com.platform.catalog.domain.enums.CatalogPropertyType;
import com.platform.catalog.repository.CatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CatalogCore {

    private final CatalogRepository catalogRepository;
    private final UniqueCatalogSlugGenerator slugGenerator;

    public CatalogCore(
        CatalogRepository catalogRepository,
        UniqueCatalogSlugGenerator slugGenerator
    ) {
        this.catalogRepository = catalogRepository;
        this.slugGenerator = slugGenerator;
    }

    @Transactional
    public Result<CatalogEntity> create(UUID ownerId, CreateCatalogRequest request) {
        CatalogEntity catalog = new CatalogEntity();
        try {
            Instant now = Instant.now();
            catalog.setId(UUID.randomUUID());
            catalog.setOwnerId(ownerId);
            catalog.setSlug(slugGenerator.generate(request.name()));
            catalog.setName(CatalogOperations.normalizeName(request.name(), 120, "Catalog name"));
            catalog.setProperties(normalizeProperties(request.properties()));
            catalog.setCreatedAt(now);
            catalog.setUpdatedAt(now);
        } catch (CatalogException exception) {
            return Result.conflict(exception.error().name(), exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_CATALOG", exception.getMessage());
        }
        catalogRepository.persist(catalog);
        return Result.created(catalog);
    }

    public Result<List<CatalogEntity>> listOwned(UUID ownerId) {
        return Result.ok(catalogRepository.findAllByOwner(ownerId));
    }

    public Result<CatalogEntity> getOwned(UUID catalogId, UUID ownerId) {
        return requireOwned(catalogRepository, catalogId, ownerId);
    }

    /**
     * Object keys for the catalog card cover strip: the most recently updated
     * visible items that have an image, newest first, capped at three.
     */
    public Result<List<String>> previewImageKeys(UUID catalogId, UUID ownerId) {
        Result<CatalogEntity> catalogResult = requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        List<String> keys = catalogResult.getValue().getItems().stream()
            .filter(CatalogItemEntity::isVisible)
            .filter(item -> item.getImageObjectKey() != null)
            .sorted(Comparator.comparing(CatalogItemEntity::getUpdatedAt).reversed())
            .limit(3)
            .map(CatalogItemEntity::getImageObjectKey)
            .toList();
        return Result.ok(keys);
    }

    public Result<CatalogEntity> getPublic(String slug) {
        return catalogRepository.findBySlug(slug)
            .map(Result::ok)
            .orElseGet(() -> Result.notFound("CATALOG_NOT_FOUND", "Catalog not found."));
    }

    /**
     * Resolves a catalog UUID to its current public slug. Backs the permanent
     * {@code /r/{id}} QR redirect: legacy printed QR codes encode the immutable
     * catalog UUID so they keep working even after the slug-based URL changes.
     */
    public Result<String> resolvePublicSlug(UUID catalogId) {
        return catalogRepository.findByIdOptional(catalogId)
            .map(catalog -> Result.ok(catalog.getSlug()))
            .orElseGet(() -> Result.notFound("CATALOG_NOT_FOUND", "Catalog not found."));
    }

    @Transactional
    public Result<CatalogEntity> updateDetails(UUID catalogId, UUID ownerId, UpdateCatalogRequest request) {
        Result<CatalogEntity> catalogResult = requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult;
        }
        CatalogEntity catalog = catalogResult.getValue();
        try {
            catalog.setName(CatalogOperations.normalizeName(request.name(), 120, "Catalog name"));
            catalog.setProperties(normalizeProperties(request.properties()));
            catalog.setUpdatedAt(Instant.now());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_CATALOG", exception.getMessage());
        }
        return Result.ok(catalog);
    }

    private static List<CatalogProperty> normalizeProperties(List<CatalogProperty> properties) {
        if (properties == null) {
            return List.of();
        }
        return properties.stream()
            .map(CatalogCore::normalizeProperty)
            .toList();
    }

    private static CatalogProperty normalizeProperty(CatalogProperty property) {
        if (property == null) {
            throw new IllegalArgumentException("Catalog property is required.");
        }
        String name = CatalogOperations.normalizeName(property.getName(), 80, "Catalog property name");
        CatalogPropertyType type = property.getType();
        if (type == null) {
            throw new IllegalArgumentException("Catalog property type is required.");
        }
        String value = CatalogOperations.normalizeName(property.getValue(), 1000, "Catalog property value");
        return new CatalogProperty(name, type, value);
    }

    @Transactional
    public Result<Void> delete(UUID catalogId, UUID ownerId) {
        Result<CatalogEntity> catalogResult = requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        catalogRepository.delete(catalogResult.getValue());
        return Result.noContent();
    }

    static Result<CatalogEntity> requireOwned(CatalogRepository catalogRepository, UUID catalogId, UUID ownerId) {
        return catalogRepository.findByIdAndOwnerWithSections(catalogId, ownerId)
            .map(Result::ok)
            .orElseGet(() -> Result.notFound("CATALOG_NOT_FOUND", "Catalog not found."));
    }
}