package com.platform.core.catalog;

import com.platform.domain.entities.catalog.CatalogSectionEntity;
import com.platform.domain.entities.catalog.CatalogEntity;
import com.platform.domain.entities.catalog.CatalogItemEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/**
 * Entity-graph mutations and invariants for a catalog and its sections and items.
 * Invariant violations are signalled with {@link IllegalArgumentException}; the
 * cores translate those into the appropriate {@code Result} error codes.
 */
final class CatalogOperations {

    private CatalogOperations() {
    }

    // ---- lookups ----

    static boolean hasSection(CatalogEntity catalog, UUID sectionId) {
        return catalog.getSections().stream()
            .anyMatch(section -> section.getId().equals(sectionId));
    }

    static CatalogSectionEntity requireSection(CatalogEntity catalog, UUID sectionId) {
        Objects.requireNonNull(sectionId, "Section ID is required.");
        return catalog.getSections().stream()
            .filter(section -> section.getId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Section not found."));
    }

    static boolean hasItem(CatalogEntity catalog, UUID itemId) {
        return catalog.getItems().stream().anyMatch(item -> item.getId().equals(itemId));
    }

    static CatalogItemEntity requireItem(CatalogEntity catalog, UUID itemId) {
        Objects.requireNonNull(itemId, "Catalog item ID is required.");
        return catalog.getItems().stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Catalog item not found."));
    }

    static UUID sectionIdOf(CatalogItemEntity item) {
        return item.getSection() == null ? null : item.getSection().getId();
    }

    static List<CatalogSectionEntity> sectionsByPosition(CatalogEntity catalog) {
        return catalog.getSections().stream()
            .sorted(Comparator.comparingInt(CatalogSectionEntity::getPosition))
            .toList();
    }

    static List<CatalogItemEntity> itemsInSection(CatalogEntity catalog, UUID sectionId) {
        return catalog.getItems().stream()
            .filter(item -> Objects.equals(sectionIdOf(item), sectionId))
            .sorted(Comparator.comparingInt(CatalogItemEntity::getPosition))
            .toList();
    }

    // ---- section operations ----

    static CatalogSectionEntity addSection(CatalogEntity catalog, String name, Instant now) {
        CatalogSectionEntity section = new CatalogSectionEntity();
        section.setId(UUID.randomUUID());
        section.setCatalog(catalog);
        section.setName(normalizeName(name, 120, "Section name"));
        section.setPosition(catalog.getSections().size());
        section.setCreatedAt(now);
        section.setUpdatedAt(now);
        catalog.getSections().add(section);
        catalog.setUpdatedAt(now);
        return section;
    }

    static void renameSection(CatalogEntity catalog, UUID sectionId, String name, Instant now) {
        CatalogSectionEntity section = requireSection(catalog, sectionId);
        section.setName(normalizeName(name, 120, "Section name"));
        section.setUpdatedAt(now);
        catalog.setUpdatedAt(now);
    }

    static void removeSection(CatalogEntity catalog, UUID sectionId, Instant now) {
        CatalogSectionEntity section = requireSection(catalog, sectionId);
        catalog.getSections().remove(section);
        catalog.getItems().stream()
            .filter(item -> sectionId.equals(sectionIdOf(item)))
            .forEach(item -> {
                item.setSection(null);
                item.setUpdatedAt(now);
            });
        normalizeSectionPositions(catalog, now);
        normalizeAllItemPositions(catalog, now);
        catalog.setUpdatedAt(now);
    }

    static void reorderCatalogSections(CatalogEntity catalog, List<UUID> orderedIds, Instant now) {
        Objects.requireNonNull(orderedIds, "Section order is required.");
        List<CatalogSectionEntity> sections = catalog.getSections();
        if (orderedIds.size() != sections.size()
            || new HashSet<>(orderedIds).size() != sections.size()
            || !orderedIds.stream().allMatch(id -> hasSection(catalog, id))) {
            throw new IllegalArgumentException(
                "Section order must contain every section exactly once."
            );
        }
        for (int position = 0; position < orderedIds.size(); position++) {
            CatalogSectionEntity section = requireSection(catalog, orderedIds.get(position));
            if (section.getPosition() != position) {
                section.setPosition(position);
                section.setUpdatedAt(now);
            }
        }
        catalog.setUpdatedAt(now);
    }

    // ---- item operations ----

    static CatalogItemEntity addItem(
        CatalogEntity catalog,
        UUID sectionId,
        String name,
        String description,
        BigDecimal priceAmount,
        boolean visible,
        boolean soldOut,
        Instant now
    ) {
        CatalogSectionEntity section = sectionId == null ? null : requireSection(catalog, sectionId);
        CatalogItemEntity item = new CatalogItemEntity();
        item.setId(UUID.randomUUID());
        item.setCatalog(catalog);
        item.setSection(section);
        item.setName(normalizeName(name, 160, "Item name"));
        item.setDescription(normalizeDescription(description));
        item.setPriceAmount(normalizePrice(priceAmount));
        item.setVisible(visible);
        item.setSoldOut(soldOut);
        item.setPosition(itemCount(catalog, sectionId, null));
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        catalog.getItems().add(item);
        catalog.setUpdatedAt(now);
        return item;
    }

    static void updateItem(
        CatalogEntity catalog,
        UUID itemId,
        UUID sectionId,
        String name,
        String description,
        BigDecimal priceAmount,
        boolean visible,
        boolean soldOut,
        Instant now
    ) {
        CatalogItemEntity item = requireItem(catalog, itemId);
        UUID previousSectionId = sectionIdOf(item);
        CatalogSectionEntity section = sectionId == null ? null : requireSection(catalog, sectionId);
        item.setSection(section);
        item.setName(normalizeName(name, 160, "Item name"));
        item.setDescription(normalizeDescription(description));
        item.setPriceAmount(normalizePrice(priceAmount));
        item.setVisible(visible);
        item.setSoldOut(soldOut);
        item.setUpdatedAt(now);
        if (!Objects.equals(previousSectionId, sectionId)) {
            item.setPosition(itemCount(catalog, sectionId, itemId));
            normalizeItemPositions(catalog, previousSectionId, now);
        }
        normalizeItemPositions(catalog, sectionId, now);
        catalog.setUpdatedAt(now);
    }

    static void removeItem(CatalogEntity catalog, UUID itemId, Instant now) {
        CatalogItemEntity item = requireItem(catalog, itemId);
        UUID sectionId = sectionIdOf(item);
        catalog.getItems().remove(item);
        normalizeItemPositions(catalog, sectionId, now);
        catalog.setUpdatedAt(now);
    }

    static void reorderItems(CatalogEntity catalog, UUID sectionId, List<UUID> orderedItemIds, Instant now) {
        Objects.requireNonNull(orderedItemIds, "Item order is required.");
        List<CatalogItemEntity> groupItems = itemsInSection(catalog, sectionId);
        if (orderedItemIds.size() != groupItems.size()
            || new HashSet<>(orderedItemIds).size() != groupItems.size()
            || !orderedItemIds.stream().allMatch(id ->
                groupItems.stream().anyMatch(item -> item.getId().equals(id)))) {
            throw new IllegalArgumentException(
                "Item order must contain every item in the group exactly once."
            );
        }
        for (int position = 0; position < orderedItemIds.size(); position++) {
            CatalogItemEntity item = requireItem(catalog, orderedItemIds.get(position));
            if (item.getPosition() != position) {
                item.setPosition(position);
                item.setUpdatedAt(now);
            }
        }
        catalog.setUpdatedAt(now);
    }

    // ---- position normalization ----

    private static void normalizeSectionPositions(CatalogEntity catalog, Instant now) {
        List<CatalogSectionEntity> sorted = sectionsByPosition(catalog);
        for (int position = 0; position < sorted.size(); position++) {
            CatalogSectionEntity section = sorted.get(position);
            if (section.getPosition() != position) {
                section.setPosition(position);
                section.setUpdatedAt(now);
            }
        }
    }

    private static void normalizeAllItemPositions(CatalogEntity catalog, Instant now) {
        normalizeItemPositions(catalog, null, now);
        catalog.getSections().forEach(section ->
            normalizeItemPositions(catalog, section.getId(), now)
        );
    }

    private static void normalizeItemPositions(CatalogEntity catalog, UUID sectionId, Instant now) {
        List<CatalogItemEntity> groupItems = itemsInSection(catalog, sectionId);
        for (int position = 0; position < groupItems.size(); position++) {
            CatalogItemEntity item = groupItems.get(position);
            if (item.getPosition() != position) {
                item.setPosition(position);
                item.setUpdatedAt(now);
            }
        }
    }

    private static int itemCount(CatalogEntity catalog, UUID sectionId, UUID excludedItemId) {
        return (int) catalog.getItems().stream()
            .filter(item -> Objects.equals(sectionIdOf(item), sectionId))
            .filter(item -> !item.getId().equals(excludedItemId))
            .count();
    }

    // ---- field normalization ----

    static String normalizeName(String name, int maximumLength, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        String normalized = name.trim();
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(
                fieldName + " cannot exceed " + maximumLength + " characters."
            );
        }
        return normalized;
    }

    static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String normalized = description.trim();
        if (normalized.length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters.");
        }
        return normalized;
    }

    static String normalizeContactField(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    static BigDecimal normalizePrice(BigDecimal amount) {
        Objects.requireNonNull(amount, "Price amount is required.");
        BigDecimal normalized = amount.setScale(2, RoundingMode.UNNECESSARY);
        if (normalized.signum() < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative.");
        }
        return normalized;
    }
}
