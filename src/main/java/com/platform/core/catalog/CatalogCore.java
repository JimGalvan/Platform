package com.platform.core.catalog;

import com.platform.common.Result;
import com.platform.common.exception.CatalogException;
import com.platform.core.catalog.creation.UniqueCatalogSlugGenerator;
import com.platform.domain.dto.catalog.CreateCatalogRequest;
import com.platform.domain.dto.catalog.UpdateCatalogRequest;
import com.platform.domain.entities.catalog.CatalogEntity;
import com.platform.domain.entities.catalog.CatalogItemEntity;
import com.platform.domain.enums.catalog.Market;
import com.platform.repository.accounts.UserRepository;
import com.platform.repository.catalog.CatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CatalogCore {

    private final CatalogRepository catalogRepository;
    private final UserRepository userRepository;
    private final UniqueCatalogSlugGenerator slugGenerator;

    public CatalogCore(
        CatalogRepository catalogRepository,
        UserRepository userRepository,
        UniqueCatalogSlugGenerator slugGenerator
    ) {
        this.catalogRepository = catalogRepository;
        this.userRepository = userRepository;
        this.slugGenerator = slugGenerator;
    }

    @Transactional
    public Result<CatalogEntity> create(UUID ownerId, CreateCatalogRequest request) {
        Market market;
        try {
            market = Market.from(request.market());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_MARKET", exception.getMessage());
        }

        CatalogEntity catalog = new CatalogEntity();
        try {
            Instant now = Instant.now();
            catalog.setId(UUID.randomUUID());
            catalog.setOwnerId(ownerId);
            catalog.setSlug(slugGenerator.generate(request.name()));
            catalog.setName(CatalogOperations.normalizeName(request.name(), 120, "Catalog name"));
            catalog.setDescription(CatalogOperations.normalizeDescription(request.description()));
            catalog.setMarket(market.name());
            catalog.setCurrency(market.currency().name());
            catalog.setPhone(CatalogOperations.normalizeContactField(request.phone()));
            catalog.setAddress(CatalogOperations.normalizeContactField(request.address()));
            catalog.setOperatingHours(CatalogOperations.normalizeContactField(request.operatingHours()));
            catalog.setShowEmail(request.showEmail());
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
     * The owner's account email to surface on the public catalog, or {@code null}
     * when the owner has not opted in via {@code showEmail}. Kept here (not in the
     * response DTO) so an unauthenticated diner never sees the email unless the
     * catalog explicitly enabled it.
     */
    public String publicContactEmail(CatalogEntity catalog) {
        if (!catalog.isShowEmail()) {
            return null;
        }
        return userRepository.findByIdOptional(catalog.getOwnerId())
            .map(owner -> owner.getEmail())
            .orElse(null);
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
            catalog.setDescription(CatalogOperations.normalizeDescription(request.description()));
            catalog.setPhone(CatalogOperations.normalizeContactField(request.phone()));
            catalog.setAddress(CatalogOperations.normalizeContactField(request.address()));
            catalog.setOperatingHours(CatalogOperations.normalizeContactField(request.operatingHours()));
            catalog.setShowEmail(request.showEmail());
            catalog.setUpdatedAt(Instant.now());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_CATALOG", exception.getMessage());
        }
        return Result.ok(catalog);
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
        return catalogRepository.findByIdAndOwner(catalogId, ownerId)
            .map(Result::ok)
            .orElseGet(() -> Result.notFound("CATALOG_NOT_FOUND", "Catalog not found."));
    }
}
