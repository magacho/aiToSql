package com.magacho.aiToSql.service;

import com.magacho.aiToSql.dto.SchemaStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * Schema Introspection Service
 * Provides database schema structure information to LLM agents
 */
@Service
public class SchemaIntrospectionService {

    private static final Logger log = LoggerFactory.getLogger(SchemaIntrospectionService.class);
    private final DataSource dataSource;

    public SchemaIntrospectionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Get complete schema structure with all tables and columns
     * This is the main tool for LLM to understand the database model
     */
    @Cacheable("schema-structure")
    public SchemaStructure getSchemaStructure(String databaseName) throws SQLException {
        log.info("Retrieving schema structure for database: {}", databaseName);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            String databaseType = metaData.getDatabaseProductName();
            String catalog = connection.getCatalog();
            String schemaPattern = connection.getSchema();

            log.info("Database type detected: {}", databaseType);

            List<SchemaStructure.TableInfo> tables = new ArrayList<>();

            ResultSet tablesRs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE", "VIEW"});

            while (tablesRs.next()) {
                String tableName = tablesRs.getString("TABLE_NAME");
                String tableType = tablesRs.getString("TABLE_TYPE");

                List<SchemaStructure.ColumnInfo> columns = getColumnsForTable(
                        metaData, catalog, schemaPattern, tableName);

                tables.add(new SchemaStructure.TableInfo(tableName, tableType, columns));
            }
            tablesRs.close();

            log.info("Schema structure retrieved: {} tables found", tables.size());

            return new SchemaStructure(
                    catalog != null ? catalog : schemaPattern,
                    databaseType,
                    tables
            );
        }
    }

    private List<SchemaStructure.ColumnInfo> getColumnsForTable(
            DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {

        List<SchemaStructure.ColumnInfo> columns = new ArrayList<>();
        Set<String> primaryKeys = getPrimaryKeys(metaData, catalog, schema, tableName);

        ResultSet columnsRs = metaData.getColumns(catalog, schema, tableName, "%");

        while (columnsRs.next()) {
            String columnName = columnsRs.getString("COLUMN_NAME");
            String dataType = columnsRs.getString("TYPE_NAME");
            Integer columnSize = columnsRs.getInt("COLUMN_SIZE");
            boolean nullable = "YES".equals(columnsRs.getString("IS_NULLABLE"));
            boolean isPK = primaryKeys.contains(columnName);

            columns.add(new SchemaStructure.ColumnInfo(
                    columnName, dataType, columnSize, nullable, isPK));
        }
        columnsRs.close();

        return columns;
    }

    private Set<String> getPrimaryKeys(
            DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {

        Set<String> primaryKeys = new HashSet<>();
        ResultSet pkRs = metaData.getPrimaryKeys(catalog, schema, tableName);

        while (pkRs.next()) {
            primaryKeys.add(pkRs.getString("COLUMN_NAME"));
        }
        pkRs.close();

        return primaryKeys;
    }
}
