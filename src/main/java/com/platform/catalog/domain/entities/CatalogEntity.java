package com.platform.catalog.domain.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "catalogs")
public class CatalogEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false, unique = true, updatable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 120)
    private String name;

    @ElementCollection
    @CollectionTable(name = "catalog_properties", joinColumns = @JoinColumn(name = "catalog_id"))
    @OrderColumn(name = "position")
    private List<CatalogProperty> properties = new ArrayList<>();

    @OneToMany(mappedBy = "catalog", cascade = ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<CatalogSectionEntity> sections = new ArrayList<>();

    @OneToMany(mappedBy = "catalog", cascade = ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<CatalogItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public CatalogEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CatalogProperty> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        return properties;
    }

    public void setProperties(List<CatalogProperty> properties) {
        this.properties = properties == null ? new ArrayList<>() : new ArrayList<>(properties);
    }

    public Optional<CatalogProperty> property(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return getProperties().stream()
            .filter(property -> name.equalsIgnoreCase(property.getName()))
            .findFirst();
    }

    public void upsertProperty(CatalogProperty property) {
        property(property.getName()).ifPresentOrElse(
            existing -> {
                existing.setType(property.getType());
                existing.setValue(property.getValue());
            },
            () -> getProperties().add(property)
        );
    }

    public List<CatalogSectionEntity> getSections() {
        return sections;
    }

    public List<CatalogItemEntity> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}