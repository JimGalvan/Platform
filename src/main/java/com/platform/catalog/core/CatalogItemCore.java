package com.platform.catalog.core;

import com.platform.common.Result;
import com.platform.catalog.domain.dto.CatalogItemRequest;
import com.platform.catalog.domain.dto.ReorderCatalogItemsRequest;
import com.platform.catalog.domain.entities.CatalogEntity;
import com.platform.catalog.domain.entities.CatalogItemEntity;
import com.platform.catalog.repository.CatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CatalogItemCore {

    private final CatalogRepository catalogRepository;

    public CatalogItemCore(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Transactional
    public Result<CatalogItemEntity> create(UUID catalogId, UUID ownerId, CatalogItemRequest request) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        Result<Void> sectionResult = requireValidSection(catalog, request.sectionId());
        if (!sectionResult.isSuccess()) {
            return sectionResult.asError();
        }
        try {
            CatalogItemEntity item = CatalogOperations.addItem(
                catalog,
                request.sectionId(),
                request.name(),
                request.description(),
                request.priceAmount(),
                request.visibleOrDefault(),
                request.soldOutOrDefault(),
                Instant.now()
            );
            return Result.created(item);
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_ITEM", exception.getMessage());
        }
    }

    @Transactional
    public Result<CatalogItemEntity> update(
        UUID catalogId,
        UUID ownerId,
        UUID itemId,
        CatalogItemRequest request
    ) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        Result<Void> itemResult = requireItem(catalog, itemId);
        if (!itemResult.isSuccess()) {
            return itemResult.asError();
        }
        Result<Void> sectionResult = requireValidSection(catalog, request.sectionId());
        if (!sectionResult.isSuccess()) {
            return sectionResult.asError();
        }
        try {
            CatalogOperations.updateItem(
                catalog,
                itemId,
                request.sectionId(),
                request.name(),
                request.description(),
                request.priceAmount(),
                request.visibleOrDefault(),
                request.soldOutOrDefault(),
                Instant.now()
            );
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_ITEM", exception.getMessage());
        }
        return Result.ok(CatalogOperations.requireItem(catalog, itemId));
    }

    @Transactional
    public Result<Void> delete(UUID catalogId, UUID ownerId, UUID itemId) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        Result<Void> itemResult = requireItem(catalog, itemId);
        if (!itemResult.isSuccess()) {
            return itemResult;
        }
        CatalogOperations.removeItem(catalog, itemId, Instant.now());
        return Result.noContent();
    }

    @Transactional
    public Result<List<CatalogItemEntity>> reorder(UUID catalogId, UUID ownerId, ReorderCatalogItemsRequest request) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        Result<Void> sectionResult = requireValidSection(catalog, request.sectionId());
        if (!sectionResult.isSuccess()) {
            return sectionResult.asError();
        }
        try {
            CatalogOperations.reorderItems(catalog, request.sectionId(), request.itemIds(), Instant.now());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_ITEM_ORDER", exception.getMessage());
        }
        return Result.ok(CatalogOperations.itemsInSection(catalog, request.sectionId()));
    }

    private static Result<Void> requireValidSection(CatalogEntity catalog, UUID sectionId) {
        if (sectionId != null && !CatalogOperations.hasSection(catalog, sectionId)) {
            return Result.unprocessableEntity(
                "INVALID_ITEM_SECTION",
                "Item section must belong to the catalog."
            );
        }
        return Result.ok(null);
    }

    static Result<Void> requireItem(CatalogEntity catalog, UUID itemId) {
        if (!CatalogOperations.hasItem(catalog, itemId)) {
            return Result.notFound("ITEM_NOT_FOUND", "Catalog item not found.");
        }
        return Result.ok(null);
    }
}
