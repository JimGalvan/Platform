package com.platform.catalog.controllers;

import com.platform.common.Result;
import com.platform.common.storage.ObjectStorage;
import com.platform.common.controller.BaseController;
import com.platform.catalog.core.CatalogCore;
import com.platform.catalog.domain.dto.*;
import com.platform.catalog.domain.entities.CatalogEntity;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/catalogs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@RunOnVirtualThread
public class CatalogController extends BaseController {

    private final CatalogCore catalogCore;
    private final ObjectStorage objectStorage;
    private final JsonWebToken jsonWebToken;

    public CatalogController(
        CatalogCore catalogCore,
        ObjectStorage objectStorage,
        JsonWebToken jsonWebToken
    ) {
        this.catalogCore = catalogCore;
        this.objectStorage = objectStorage;
        this.jsonWebToken = jsonWebToken;
    }

    @POST
    public Response create(@Valid CreateCatalogRequest request) {
        Result<CatalogEntity> result = catalogCore.create(getAuthenticatedUserId(jsonWebToken), request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.created(CatalogResponse.from(result.getValue(), objectStorage)));
    }

    @GET
    public Response list() {
        Result<List<CatalogEntity>> result = catalogCore.listOwned(getAuthenticatedUserId(jsonWebToken));
        return toResponse(Result.ok(result.getValue().stream()
            .map(catalog -> CatalogSummaryResponse.from(catalog, objectStorage))
            .toList()));
    }

    @GET
    @Path("/{catalogId}")
    @Transactional
    public Response get(@PathParam("catalogId") UUID catalogId) {
        Result<CatalogEntity> result = catalogCore.getOwned(catalogId, getAuthenticatedUserId(jsonWebToken));
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogResponse.from(result.getValue(), objectStorage)));
    }

    @GET
    @Path("/{catalogId}/preview-images")
    @Transactional
    public Response previewImages(@PathParam("catalogId") UUID catalogId) {
        Result<List<String>> result = catalogCore.previewImageKeys(catalogId, getAuthenticatedUserId(jsonWebToken));
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(
            CatalogPreviewImagesResponse.from(result.getValue(), objectStorage)));
    }

    @PATCH
    @Path("/{catalogId}")
    @Transactional
    public Response update(
        @PathParam("catalogId") UUID catalogId,
        @Valid UpdateCatalogRequest request
    ) {
        Result<CatalogEntity> result = catalogCore.updateDetails(catalogId, getAuthenticatedUserId(jsonWebToken), request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogResponse.from(result.getValue(), objectStorage)));
    }

    @PUT
    @Path("/{catalogId}/socials")
    @Transactional
    public Response updateSocials(
        @PathParam("catalogId") UUID catalogId,
        List<SocialLink> socials
    ) {
        Result<CatalogEntity> result = catalogCore.updateSocials(catalogId, getAuthenticatedUserId(jsonWebToken), socials);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogResponse.from(result.getValue(), objectStorage)));
    }

    @PUT
    @Path("/{catalogId}/accent")
    @Transactional
    public Response updateAccent(
        @PathParam("catalogId") UUID catalogId,
        @Valid UpdateAccentRequest request
    ) {
        Result<CatalogEntity> result = catalogCore.updateAccent(catalogId, getAuthenticatedUserId(jsonWebToken), request.accentColor());
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogResponse.from(result.getValue(), objectStorage)));
    }

    @DELETE
    @Path("/{catalogId}")
    public Response delete(@PathParam("catalogId") UUID catalogId) {
        return toResponse(catalogCore.delete(catalogId, getAuthenticatedUserId(jsonWebToken)));
    }
}
