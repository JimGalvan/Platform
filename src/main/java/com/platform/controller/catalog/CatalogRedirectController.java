package com.platform.controller.catalog;

import com.platform.common.Result;
import com.platform.core.catalog.CatalogCore;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.UUID;

/**
 * Permanent QR redirect. Every QR code the legacy Django app ever printed encodes
 * {@code https://platform.example/r/<catalog-uuid>/} — an immutable UUID link, not the
 * slug URL — so that codes in the wild keep working even when the slug changes.
 *
 * <p>This resource is the faithful replacement for Django's
 * {@code catalog_shortcode_redirect}: resolve the UUID to the current slug and 302 to
 * the public catalog page. It lives at the bare {@code /r/{id}} path (NOT under
 * {@code /api/v1}) because that path is baked into physical, already-printed codes
 * and can never change.
 *
 * <p>The redirect is a {@code 302} (never {@code 301}): the target slug can change
 * over time, and a cached permanent redirect would defeat the whole reason these
 * links encode the UUID.
 */
@Path("/r")
@RunOnVirtualThread
public class CatalogRedirectController {

    private final CatalogCore catalogCore;

    public CatalogRedirectController(CatalogCore catalogCore) {
        this.catalogCore = catalogCore;
    }

    @GET
    @Path("/{id}")
    public Response redirect(@PathParam("id") UUID id) {
        Result<String> result = catalogCore.resolvePublicSlug(id);
        if (!result.isSuccess()) {
            // A human scanned this — fail soft to the landing page rather than an
            // API-style JSON 404. Happens only for deleted catalogs.
            return Response.status(Response.Status.FOUND)
                .location(UriBuilder.fromUri("/").build())
                .build();
        }
        return Response.status(Response.Status.FOUND)
            .location(UriBuilder.fromUri("/m/{slug}").build(result.getValue()))
            .build();
    }
}
