package com.platform.catalog.controllers;

import com.platform.common.Result;
import com.platform.common.controller.BaseController;
import com.platform.catalog.core.CatalogSectionCore;
import com.platform.catalog.domain.dto.CatalogSectionRequest;
import com.platform.catalog.domain.dto.CatalogSectionResponse;
import com.platform.catalog.domain.dto.ReorderCatalogSectionsRequest;
import com.platform.catalog.domain.entities.CatalogSectionEntity;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/catalogs/{catalogId}/sections")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@RunOnVirtualThread
public class CatalogSectionController extends BaseController {

    private final CatalogSectionCore sectionCore;
    private final JsonWebToken jsonWebToken;

    public CatalogSectionController(CatalogSectionCore sectionCore, JsonWebToken jsonWebToken) {
        this.sectionCore = sectionCore;
        this.jsonWebToken = jsonWebToken;
    }

    @POST
    public Response add(@PathParam("catalogId") UUID catalogId, @Valid CatalogSectionRequest request) {
        Result<CatalogSectionEntity> result = sectionCore.add(catalogId, getAuthenticatedUserId(jsonWebToken), request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.created(CatalogSectionResponse.from(result.getValue())));
    }

    @PATCH
    @Path("/{sectionId}")
    public Response rename(
        @PathParam("catalogId") UUID catalogId,
        @PathParam("sectionId") UUID sectionId,
        @Valid CatalogSectionRequest request
    ) {
        Result<CatalogSectionEntity> result = sectionCore.rename(catalogId, getAuthenticatedUserId(jsonWebToken), sectionId, request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(CatalogSectionResponse.from(result.getValue())));
    }

    @DELETE
    @Path("/{sectionId}")
    public Response delete(@PathParam("catalogId") UUID catalogId, @PathParam("sectionId") UUID sectionId) {
        return toResponse(sectionCore.delete(catalogId, getAuthenticatedUserId(jsonWebToken), sectionId));
    }

    @PUT
    @Path("/order")
    public Response reorder(@PathParam("catalogId") UUID catalogId, @Valid ReorderCatalogSectionsRequest request) {
        Result<List<CatalogSectionEntity>> result = sectionCore.reorder(catalogId, getAuthenticatedUserId(jsonWebToken), request);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        return toResponse(Result.ok(result.getValue().stream().map(CatalogSectionResponse::from).toList()));
    }
}
