package com.platform.controller.catalog;

import com.platform.common.Result;
import com.platform.common.storage.ObjectStorage;
import com.platform.controller.BaseController;
import com.platform.core.catalog.CatalogCore;
import com.platform.domain.dto.catalog.PublicCatalogResponse;
import com.platform.domain.entities.catalog.CatalogEntity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/public/catalogs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class PublicCatalogController extends BaseController {

    private final CatalogCore catalogCore;
    private final ObjectStorage objectStorage;

    public PublicCatalogController(
        CatalogCore catalogCore,
        ObjectStorage objectStorage
    ) {
        this.catalogCore = catalogCore;
        this.objectStorage = objectStorage;
    }

    @GET
    @Path("/{slug}")
    public Response get(@PathParam("slug") String slug) {
        Result<CatalogEntity> result = catalogCore.getPublic(slug);
        if (!result.isSuccess()) {
            return toResponse(result);
        }
        CatalogEntity catalog = result.getValue();
        return toResponse(Result.ok(
            PublicCatalogResponse.from(catalog, objectStorage, catalogCore.publicContactEmail(catalog))));
    }
}
