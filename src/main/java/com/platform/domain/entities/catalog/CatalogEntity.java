package com.platform.domain.entities.catalog;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, updatable = false, length = 2)
    private String market;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Column(name = "logo_object_key", length = 1024)
    private String logoObjectKey;

    @Column(length = 50)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(name = "operating_hours", length = 1000)
    private String operatingHours;

    @Column(name = "show_email", nullable = false)
    private boolean showEmail;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public boolean isShowEmail() {
        return showEmail;
    }

    public void setShowEmail(boolean showEmail) {
        this.showEmail = showEmail;
    }

    public String getLogoObjectKey() {
        return logoObjectKey;
    }

    public void setLogoObjectKey(String logoObjectKey) {
        this.logoObjectKey = logoObjectKey;
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
