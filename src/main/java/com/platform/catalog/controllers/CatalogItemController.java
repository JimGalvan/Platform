package com.platform.catalog.controllers;

import com.platform.common.Result;
import com.platform.common.storage.ObjectStorage;
import com.platform.common.controller.BaseController;
import com.platform.catalog.core.CatalogItemCore;
import com.platform.catalog.domain.dto.CatalogItemRequest;
import com.platform.catalog.domain.dto.CatalogItemResponse;
import com.platform.catalog.domain.dto.ReorderCatalogItemsRequest;
import com.platform.catalog.domain.entities.CatalogItemEntity;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/catalogs/{catalogId}/items")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@RunOnVirtualThread
public class CatalogItemController extends BaseController {

    private final CatalogItemCore catalogItemCore;
    private final ObjectStorage objectStorage;
    private final JsonWebToken jsonWebToken;

    public CatalogItemController(CatalogItemCore catalogItemCore, ObjectStorage objectStorage, JsonWebToken jsonWebToken) {
        this.catalogItemCore = catalogItemCore;
        this.objectStorage = objectStorage;
        this.jsonWebToken = jsonWebToken;
    }

    @POST
    public Response create(@PathParam("catalogId") UUID catalogId, @Valid CatalogItemRequest request) {
        Result<CatalogItemEntity> result = catalogItemCore.create(catalogId, getAuthenticatedUserId(jsonWebToken), request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.created(CatalogItemResponse.from(result.getValue(), objectStorage)));
    }

    @PATCH
    @Path("/{itemId}")
    public Response update(@PathParam("catalogId") UUID catalogId, @PathParam("itemId") UUID itemId, @Valid CatalogItemRequest request) {
        Result<CatalogItemEntity> result = catalogItemCore.update(catalogId, getAuthenticatedUserId(jsonWebToken), itemId, request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogItemResponse.from(result.getValue(), objectStorage)));
    }

    @DELETE
    @Path("/{itemId}")
    public Response delete(@PathParam("catalogId") UUID catalogId, @PathParam("itemId") UUID itemId) {
        return toResponse(catalogItemCore.delete(catalogId, getAuthenticatedUserId(jsonWebToken), itemId));
    }

    @PUT
    @Path("/order")
    public Response reorder(@PathParam("catalogId") UUID catalogId, @Valid ReorderCatalogItemsRequest request) {
        Result<List<CatalogItemEntity>> result = catalogItemCore.reorder(catalogId, getAuthenticatedUserId(jsonWebToken), request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(result.getValue().stream()
            .map(item -> CatalogItemResponse.from(item, objectStorage))
            .toList()));
    }
}
