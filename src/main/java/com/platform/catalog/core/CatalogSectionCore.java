package com.platform.catalog.core;

import com.platform.common.Result;
import com.platform.catalog.domain.dto.CatalogSectionRequest;
import com.platform.catalog.domain.dto.ReorderCatalogSectionsRequest;
import com.platform.catalog.domain.entities.CatalogSectionEntity;
import com.platform.catalog.domain.entities.CatalogEntity;
import com.platform.catalog.repository.CatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CatalogSectionCore {

    private final CatalogRepository catalogRepository;

    public CatalogSectionCore(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Transactional
    public Result<CatalogSectionEntity> add(UUID catalogId, UUID ownerId, CatalogSectionRequest request) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        try {
            return Result.created(CatalogOperations.addSection(catalog, request.name(), Instant.now()));
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_SECTION", exception.getMessage());
        }
    }

    @Transactional
    public Result<CatalogSectionEntity> rename(UUID catalogId, UUID ownerId, UUID sectionId, CatalogSectionRequest request) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        if (!CatalogOperations.hasSection(catalog, sectionId)) {
            return Result.notFound("SECTION_NOT_FOUND", "Section not found.");
        }
        try {
            CatalogOperations.renameSection(catalog, sectionId, request.name(), Instant.now());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_SECTION", exception.getMessage());
        }
        return Result.ok(CatalogOperations.requireSection(catalog, sectionId));
    }

    @Transactional
    public Result<Void> delete(UUID catalogId, UUID ownerId, UUID sectionId) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        if (!CatalogOperations.hasSection(catalog, sectionId)) {
            return Result.notFound("SECTION_NOT_FOUND", "Section not found.");
        }
        CatalogOperations.removeSection(catalog, sectionId, Instant.now());
        return Result.noContent();
    }

    @Transactional
    public Result<List<CatalogSectionEntity>> reorder(UUID catalogId, UUID ownerId, ReorderCatalogSectionsRequest request) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        try {
            CatalogOperations.reorderCatalogSections(catalog, request.sectionIds(), Instant.now());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_SECTION_ORDER", exception.getMessage());
        }
        return Result.ok(CatalogOperations.sectionsByPosition(catalog));
    }
}
