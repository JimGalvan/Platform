package com.platform.repository.catalog;

import com.platform.domain.entities.catalog.CatalogEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CatalogRepository implements PanacheRepositoryBase<CatalogEntity, UUID> {

    public Optional<CatalogEntity> findByIdAndOwner(UUID catalogId, UUID ownerId) {
        return find("id = ?1 and ownerId = ?2", catalogId, ownerId).firstResultOptional();
    }

    public Optional<CatalogEntity> findBySlug(String slug) {
        return find("slug", slug).firstResultOptional();
    }

    public List<CatalogEntity> findAllByOwner(UUID ownerId) {
        return find("ownerId = ?1 order by updatedAt desc, name", ownerId).list();
    }

    public boolean existsBySlug(String slug) {
        return count("slug", slug) > 0;
    }
}
