package com.platform.catalog.repository;

import com.platform.catalog.domain.entities.CatalogEntity;
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

    public Optional<CatalogEntity> findByIdAndOwnerWithSections(UUID catalogId, UUID ownerId) {
        return find("""
        select distinct c
        from CatalogEntity c
        left join fetch c.sections
        where c.id = ?1 and c.ownerId = ?2
        """, catalogId, ownerId)
                .firstResultOptional();
    }

    public boolean existsBySlug(String slug) {
        return count("slug", slug) > 0;
    }
}
