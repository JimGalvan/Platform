package com.platform.controller.catalog;

import com.platform.common.Result;
import com.platform.common.storage.ObjectStorage;
import com.platform.controller.BaseController;
import com.platform.core.catalog.CatalogMediaCore;
import com.platform.domain.dto.catalog.ImageUpload;
import com.platform.domain.dto.catalog.CatalogItemResponse;
import com.platform.domain.dto.catalog.CatalogResponse;
import com.platform.domain.entities.catalog.CatalogEntity;
import com.platform.domain.entities.catalog.CatalogItemEntity;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Path("/api/v1/catalogs/{catalogId}")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@RunOnVirtualThread
public class MediaController extends BaseController {

    private final CatalogMediaCore catalogMediaCore;
    private final ObjectStorage objectStorage;
    private final JsonWebToken jsonWebToken;

    public MediaController(
        CatalogMediaCore catalogMediaCore,
        ObjectStorage objectStorage,
        JsonWebToken jsonWebToken
    ) {
        this.catalogMediaCore = catalogMediaCore;
        this.objectStorage = objectStorage;
        this.jsonWebToken = jsonWebToken;
    }

    @POST
    @Path("/logo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadLogo(
        @PathParam("catalogId") UUID catalogId,
        @RestForm("file") FileUpload file
    ) throws IOException {
        Result<CatalogEntity> result = catalogMediaCore.uploadLogo(
            catalogId,
            getAuthenticatedUserId(jsonWebToken),
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
