package com.platform.catalog.controllers;

import com.platform.common.Result;
import com.platform.common.storage.ObjectStorage;
import com.platform.common.controller.BaseController;
import com.platform.catalog.core.CatalogMediaCore;
import com.platform.catalog.domain.dto.ImageUpload;
import com.platform.catalog.domain.dto.CatalogItemResponse;
import com.platform.catalog.domain.dto.CatalogResponse;
import com.platform.catalog.domain.entities.CatalogEntity;
import com.platform.catalog.domain.entities.CatalogItemEntity;
import com.platform.catalog.domain.keys.CatalogMediaKind;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/catalogs/{catalogId}")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@RunOnVirtualThread
public class CatalogMediaController extends BaseController {

    private final CatalogMediaCore catalogMediaCore;
    private final ObjectStorage objectStorage;
    private final JsonWebToken jsonWebToken;

    public CatalogMediaController(
        CatalogMediaCore catalogMediaCore,
        ObjectStorage objectStorage,
        JsonWebToken jsonWebToken
    ) {
        this.catalogMediaCore = catalogMediaCore;
        this.objectStorage = objectStorage;
        this.jsonWebToken = jsonWebToken;
    }

    @POST
    @Path("/media/{kind}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadMedia(
        @PathParam("catalogId") UUID catalogId,
        @PathParam("kind") String kind,
        @RestForm("file") FileUpload file
    ) throws IOException {
        Optional<CatalogMediaKind> mediaKind = CatalogMediaKind.fromSlug(kind);
        if (mediaKind.isEmpty()) {
            return toResponse(Result.notFound("UNKNOWN_MEDIA_KIND", "Unknown media kind: " + kind));
        }
        Result<CatalogEntity> result = catalogMediaCore.uploadImage(
            catalogId,
            getAuthenticatedUserId(jsonWebToken),
            mediaKind.get(),
            imageUpload(file)
        );
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogResponse.from(result.getValue(), objectStorage)));
    }

    @POST
    @Path("/items/{itemId}/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadItemImage(
        @PathParam("catalogId") UUID catalogId,
        @PathParam("itemId") UUID itemId,
        @RestForm("file") FileUpload file
    ) throws IOException {
        Result<CatalogItemEntity> result = catalogMediaCore.uploadItemImage(
            catalogId,
            getAuthenticatedUserId(jsonWebToken),
            itemId,
            imageUpload(file)
        );
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogItemResponse.from(result.getValue(), objectStorage)));
    }

    private ImageUpload imageUpload(FileUpload file) throws IOException {
        if (file == null) {
            return new ImageUpload(null, null, null);
        }
        return new ImageUpload(
            Files.readAllBytes(file.uploadedFile()),
            file.contentType(),
            file.fileName()
        );
    }
}
