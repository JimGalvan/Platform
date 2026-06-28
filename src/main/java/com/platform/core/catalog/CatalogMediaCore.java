package com.platform.core.catalog;

import com.platform.common.Result;
import com.platform.common.exception.CatalogException;
import com.platform.common.media.ImageValidator;
import com.platform.common.storage.ObjectStorage;
import com.platform.domain.dto.catalog.ImageUpload;
import com.platform.domain.entities.catalog.CatalogEntity;
import com.platform.domain.entities.catalog.CatalogItemEntity;
import com.platform.domain.model.ValidatedImage;
import com.platform.repository.catalog.CatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class CatalogMediaCore {

    private final CatalogRepository catalogRepository;
    private final ImageValidator imageValidator;
    private final ObjectStorage objectStorage;

    public CatalogMediaCore(
        CatalogRepository catalogRepository,
        ImageValidator imageValidator,
        ObjectStorage objectStorage
    ) {
        this.catalogRepository = catalogRepository;
        this.imageValidator = imageValidator;
        this.objectStorage = objectStorage;
    }

    @Transactional
    public Result<CatalogEntity> uploadLogo(UUID catalogId, UUID ownerId, ImageUpload upload) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult;
        }
        CatalogEntity catalog = catalogResult.getValue();
        Result<ValidatedImage> imageResult = validate(upload);
        if (!imageResult.isSuccess()) {
            return imageResult.asError();
        }
        ValidatedImage image = imageResult.getValue();
        String objectKey = "catalogs/" + catalog.getId() + "/logo/original." + image.extension();
        objectStorage.put(objectKey, image.bytes(), image.contentType());
        catalog.setLogoObjectKey(objectKey);
        catalog.setUpdatedAt(Instant.now());
        return Result.ok(catalog);
    }

    @Transactional
    public Result<CatalogItemEntity> uploadItemImage(
        UUID catalogId,
        UUID ownerId,
        UUID itemId,
        ImageUpload upload
    ) {
        Result<CatalogEntity> catalogResult = CatalogCore.requireOwned(catalogRepository, catalogId, ownerId);
        if (!catalogResult.isSuccess()) {
            return catalogResult.asError();
        }
        CatalogEntity catalog = catalogResult.getValue();
        Result<Void> itemResult = CatalogItemCore.requireItem(catalog, itemId);
        if (!itemResult.isSuccess()) {
            return itemResult.asError();
        }
        Result<ValidatedImage> imageResult = validate(upload);
        if (!imageResult.isSuccess()) {
            return imageResult.asError();
        }
        ValidatedImage image = imageResult.getValue();
        CatalogItemEntity item = CatalogOperations.requireItem(catalog, itemId);
        String objectKey =
            "catalogs/" + catalog.getId() + "/items/" + itemId + "/original." + image.extension();
        objectStorage.put(objectKey, image.bytes(), image.contentType());
        Instant now = Instant.now();
        item.setImageObjectKey(objectKey);
        item.setUpdatedAt(now);
        catalog.setUpdatedAt(now);
        return Result.ok(item);
    }

    private Result<ValidatedImage> validate(ImageUpload upload) {
        try {
            return Result.ok(imageValidator.validate(upload));
        } catch (CatalogException exception) {
            return Result.unprocessableEntity(exception.error().name(), exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return Result.unprocessableEntity("INVALID_IMAGE", exception.getMessage());
        }
    }
}
