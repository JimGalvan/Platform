package com.platform.catalog.domain.entities;

import com.platform.catalog.domain.enums.CatalogPropertyType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Embeddable
public class CatalogProperty {

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private CatalogPropertyType type;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String value;

    public CatalogProperty() {
    }

    public CatalogProperty(String name, CatalogPropertyType type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CatalogPropertyType getType() {
        return type;
    }

    public void setType(CatalogPropertyType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}