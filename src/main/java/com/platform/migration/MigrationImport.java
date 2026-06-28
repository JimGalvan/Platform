package com.platform.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Properties;
import java.util.UUID;

/**
 * Throwaway, dev-only importer for the Django -> Quarkus migration.
 *
 * <p>Reads the JSONL files produced by the Django {@code migration_export} command and
 * upserts them into the Quarkus PostgreSQL schema in foreign-key-safe order
 * (users -> catalogs -> catalog_sections -> catalog_items). Idempotent: every statement is a
 * primary-key upsert, so a rehearsal can be re-run and a second run is a no-op.
 *
 * <p>This is NOT part of the Quarkus runtime. It talks to the database with plain JDBC
 * so it stays decoupled from Hibernate, optimistic locking, and the application's
 * transaction boundaries, exactly as the runbook requires. PostgreSQL only: the
 * production migration target is PostgreSQL.
 *
 * <pre>
 * ./gradlew migrationImport --args="--dir /path/migration-&lt;ts&gt; \
 *     --url jdbc:postgresql://localhost:5432/platform --user platform --password secret"
 * </pre>
 */
public final class MigrationImport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MigrationImport() {
    }

    public static void main(String[] args) throws Exception {
        Args parsed = Args.parse(args);
        Properties props = new Properties();
        if (parsed.user != null) {
            props.setProperty("user", parsed.user);
        }
        if (parsed.password != null) {
            props.setProperty("password", parsed.password);
        }

        Path dir = Path.of(parsed.dir);
        Path exceptionsPath = dir.resolve("import-exceptions.jsonl");

        try (Connection conn = DriverManager.getConnection(parsed.url, props);
             BufferedWriter exceptions = Files.newBufferedWriter(
                 exceptionsPath, StandardCharsets.UTF_8)) {
            conn.setAutoCommit(false);

            int users = importUsers(conn, dir, exceptions);
            int catalogs = importCatalogs(conn, dir, exceptions);
            int catalogSections = importCatalogSections(conn, dir, exceptions);
            int items = importItems(conn, dir, exceptions);

            System.out.printf(
                "Imported: users=%d catalogs=%d sections=%d catalog_items=%d%n",
                users, catalogs, catalogSections, items);
            System.out.println("Import exceptions written to " + exceptionsPath);
        }
    }

    // -- entity loaders ---------------------------------------------------

    private static int importUsers(Connection conn, Path dir, BufferedWriter exceptions)
        throws Exception {
        String sql = """
            INSERT INTO users (id, email, password_hash, created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?, 0)
            ON CONFLICT (id) DO UPDATE SET
                email = EXCLUDED.email,
                password_hash = EXCLUDED.password_hash,
                created_at = EXCLUDED.created_at,
                updated_at = EXCLUDED.updated_at
            """;
        return load(conn, dir.resolve("users.jsonl"), sql, exceptions, (ps, row) -> {
            ps.setObject(1, uuid(row, "id"));
            ps.setString(2, text(row, "email"));
            ps.setString(3, text(row, "password_hash"));
            ps.setObject(4, timestamp(row, "created_at"));
            ps.setObject(5, timestamp(row, "updated_at"));
        });
    }

    private static int importCatalogs(Connection conn, Path dir, BufferedWriter exceptions)
        throws Exception {
        String sql = """
            INSERT INTO catalogs (id, owner_id, slug, name, description, market, currency,
                logo_object_key, phone, address, operating_hours,
                created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            ON CONFLICT (id) DO UPDATE SET
                owner_id = EXCLUDED.owner_id,
                slug = EXCLUDED.slug,
                name = EXCLUDED.name,
                description = EXCLUDED.description,
                market = EXCLUDED.market,
                currency = EXCLUDED.currency,
                logo_object_key = EXCLUDED.logo_object_key,
                phone = EXCLUDED.phone,
                address = EXCLUDED.address,
                operating_hours = EXCLUDED.operating_hours,
                created_at = EXCLUDED.created_at,
                updated_at = EXCLUDED.updated_at
            """;
        return load(conn, dir.resolve("catalogs.jsonl"), sql, exceptions, (ps, row) -> {
            ps.setObject(1, uuid(row, "id"));
            ps.setObject(2, uuid(row, "owner_id"));
            ps.setString(3, text(row, "slug"));
            ps.setString(4, text(row, "name"));
            setNullableString(ps, 5, row, "description");
            ps.setString(6, text(row, "market"));
            ps.setString(7, text(row, "currency"));
            setNullableString(ps, 8, row, "logo_object_key");
            setNullableString(ps, 9, row, "phone");
            setNullableString(ps, 10, row, "address");
            setNullableString(ps, 11, row, "operating_hours");
            ps.setObject(12, timestamp(row, "created_at"));
            ps.setObject(13, timestamp(row, "updated_at"));
        });
    }

    private static int importCatalogSections(
        Connection conn, Path dir, BufferedWriter exceptions) throws Exception {
        String sql = """
            INSERT INTO catalog_sections (id, catalog_id, name, position, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                catalog_id = EXCLUDED.catalog_id,
                name = EXCLUDED.name,
                position = EXCLUDED.position,
                created_at = EXCLUDED.created_at,
                updated_at = EXCLUDED.updated_at
            """;
        return load(conn, dir.resolve("catalog-sections.jsonl"), sql, exceptions, (ps, row) -> {
            ps.setObject(1, uuid(row, "id"));
            ps.setObject(2, uuid(row, "catalog_id"));
            ps.setString(3, text(row, "name"));
            ps.setInt(4, row.get("position").asInt());
            ps.setObject(5, timestamp(row, "created_at"));
            ps.setObject(6, timestamp(row, "updated_at"));
        });
    }

    private static int importItems(Connection conn, Path dir, BufferedWriter exceptions)
        throws Exception {
        String sql = """
            INSERT INTO catalog_items (id, catalog_id, section_id, name, description,
                price_amount, image_object_key, visible, sold_out, position,
                created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                catalog_id = EXCLUDED.catalog_id,
                section_id = EXCLUDED.section_id,
                name = EXCLUDED.name,
                description = EXCLUDED.description,
                price_amount = EXCLUDED.price_amount,
                image_object_key = EXCLUDED.image_object_key,
                visible = EXCLUDED.visible,
                sold_out = EXCLUDED.sold_out,
                position = EXCLUDED.position,
                created_at = EXCLUDED.created_at,
                updated_at = EXCLUDED.updated_at
            """;
        return load(conn, dir.resolve("catalog-items.jsonl"), sql, exceptions, (ps, row) -> {
            ps.setObject(1, uuid(row, "id"));
            ps.setObject(2, uuid(row, "catalog_id"));
            ps.setObject(3, uuid(row, "section_id"));
            ps.setString(4, text(row, "name"));
            setNullableString(ps, 5, row, "description");
            ps.setBigDecimal(6, new BigDecimal(text(row, "price_amount")));
            setNullableString(ps, 7, row, "image_object_key");
            ps.setBoolean(8, row.get("visible").asBoolean());
            ps.setBoolean(9, row.get("sold_out").asBoolean());
            ps.setInt(10, row.get("position").asInt());
            ps.setObject(11, timestamp(row, "created_at"));
            ps.setObject(12, timestamp(row, "updated_at"));
        });
    }

    // -- engine -----------------------------------------------------------

    @FunctionalInterface
    private interface RowBinder {
        void bind(PreparedStatement ps, JsonNode row) throws Exception;
    }

    private static int load(
        Connection conn, Path file, String sql,
        BufferedWriter exceptions, RowBinder binder) throws Exception {
        int imported = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                JsonNode row = MAPPER.readTree(line);
                Savepoint savepoint = conn.setSavepoint();
                try {
                    ps.clearParameters();
                    binder.bind(ps, row);
                    ps.executeUpdate();
                    conn.releaseSavepoint(savepoint);
                    imported++;
                } catch (Exception failure) {
                    conn.rollback(savepoint);
                    writeException(exceptions, file.getFileName().toString(), row, failure);
                }
            }
        }
        conn.commit();
        return imported;
    }

    private static void writeException(
        BufferedWriter exceptions, String source, JsonNode row, Exception failure)
        throws Exception {
        String id = row.hasNonNull("id") ? row.get("id").asText() : null;
        var node = MAPPER.createObjectNode();
        node.put("source", source);
        node.put("id", id);
        node.put("error", failure.getMessage());
        exceptions.write(MAPPER.writeValueAsString(node));
        exceptions.newLine();
        exceptions.flush();
    }

    // -- value helpers ----------------------------------------------------

    private static UUID uuid(JsonNode row, String field) {
        JsonNode node = row.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        return UUID.fromString(node.asText());
    }

    private static String text(JsonNode row, String field) {
        JsonNode node = row.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static OffsetDateTime timestamp(JsonNode row, String field) {
        String value = text(row, field);
        return value == null ? null : OffsetDateTime.parse(value);
    }

    private static void setNullableString(
        PreparedStatement ps, int index, JsonNode row, String field) throws SQLException {
        String value = text(row, field);
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    // -- args -------------------------------------------------------------

    private static final class Args {
        String dir;
        String url;
        String user;
        String password;

        static Args parse(String[] argv) {
            Args args = new Args();
            for (int i = 0; i < argv.length - 1; i++) {
                switch (argv[i]) {
                    case "--dir" -> args.dir = argv[++i];
                    case "--url" -> args.url = argv[++i];
                    case "--user" -> args.user = argv[++i];
                    case "--password" -> args.password = argv[++i];
                    default -> { }
                }
            }
            if (args.dir == null || args.url == null) {
                throw new IllegalArgumentException(
                    "Usage: --dir <migration-dir> --url <jdbc-url> "
                        + "[--user <user>] [--password <password>]");
            }
            return args;
        }
    }
}
