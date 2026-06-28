CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(64) UNIQUE,
    refresh_token_expiry TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE catalogs (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_catalogs_owner_id ON catalogs(owner_id);

CREATE TABLE catalog_properties (
    catalog_id UUID NOT NULL REFERENCES catalogs(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    name VARCHAR(80) NOT NULL,
    type VARCHAR(80) NOT NULL,
    value VARCHAR(1000) NOT NULL,
    PRIMARY KEY (catalog_id, position)
);

CREATE INDEX idx_catalog_properties_catalog_id ON catalog_properties(catalog_id);

CREATE TABLE catalog_sections (
    id UUID PRIMARY KEY,
    catalog_id UUID NOT NULL REFERENCES catalogs(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_catalog_sections_catalog_id ON catalog_sections(catalog_id);

CREATE TABLE catalog_items (
    id UUID PRIMARY KEY,
    catalog_id UUID NOT NULL REFERENCES catalogs(id) ON DELETE CASCADE,
    section_id UUID REFERENCES catalog_sections(id) ON DELETE SET NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    price_amount NUMERIC(10, 2) NOT NULL,
    image_object_key VARCHAR(1024),
    visible BOOLEAN NOT NULL,
    sold_out BOOLEAN NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_catalog_items_catalog_id ON catalog_items(catalog_id);
CREATE INDEX idx_catalog_items_section_id ON catalog_items(section_id);