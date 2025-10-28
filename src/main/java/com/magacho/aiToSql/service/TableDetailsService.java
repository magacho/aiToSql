package com.magacho.aiToSql.service;

import com.magacho.aiToSql.dto.TableDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * Table Details Service
 * Provides detailed information about specific tables including indexes, foreign keys, and constraints
 */
@Service
public class TableDetailsService {

    private static final Logger log = LoggerFactory.getLogger(TableDetailsService.class);
    private final DataSource dataSource;

    public TableDetailsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Get detailed information about a specific table
     */
    @Cacheable("table-details")
    public TableDetails getTableDetails(String tableName) throws SQLException {
        log.info("Retrieving detailed information for table: {}", tableName);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            String tableType = getTableType(metaData, catalog, schema, tableName);

            List<TableDetails.ColumnDetail> columns = getColumnDetails(
                    metaData, catalog, schema, tableName);

            List<TableDetails.IndexInfo> indexes = getIndexInfo(
                    metaData, catalog, schema, tableName);

            List<TableDetails.ForeignKeyInfo> foreignKeys = getForeignKeyInfo(
                    metaData, catalog, schema, tableName);

            List<TableDetails.ConstraintInfo> constraints = new ArrayList<>();

            log.info("Table details retrieved for: {}", tableName);

            return new TableDetails(tableName, tableType, columns, indexes, foreignKeys, constraints);
        }
    }

    private String getTableType(DatabaseMetaData metaData, String catalog, String schema, String tableName)
            throws SQLException {
        ResultSet rs = metaData.getTables(catalog, schema, tableName, null);
        String type = "TABLE";
        if (rs.next()) {
            type = rs.getString("TABLE_TYPE");
        }
        rs.close();
        return type;
    }

    private List<TableDetails.ColumnDetail> getColumnDetails(
            DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {

        List<TableDetails.ColumnDetail> columns = new ArrayList<>();
        Set<String> primaryKeys = getPrimaryKeys(metaData, catalog, schema, tableName);
        Set<String> autoIncrementColumns = new HashSet<>();

        ResultSet columnsRs = metaData.getColumns(catalog, schema, tableName, "%");

        while (columnsRs.next()) {
            String columnName = columnsRs.getString("COLUMN_NAME");
            String dataType = columnsRs.getString("TYPE_NAME");
            Integer columnSize = columnsRs.getInt("COLUMN_SIZE");
            Integer decimalDigits = columnsRs.getInt("DECIMAL_DIGITS");
            boolean nullable = "YES".equals(columnsRs.getString("IS_NULLABLE"));
            String defaultValue = columnsRs.getString("COLUMN_DEF");
            boolean isPK = primaryKeys.contains(columnName);
            
            String isAutoIncrement = columnsRs.getString("IS_AUTOINCREMENT");
            boolean autoIncrement = "YES".equalsIgnoreCase(isAutoIncrement);

            columns.add(new TableDetails.ColumnDetail(
                    columnName, dataType, columnSize, decimalDigits, 
                    nullable, defaultValue, isPK, autoIncrement));
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

    private List<TableDetails.IndexInfo> getIndexInfo(
            DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {

        List<TableDetails.IndexInfo> indexes = new ArrayList<>();
        ResultSet indexRs = metaData.getIndexInfo(catalog, schema, tableName, false, true);

        while (indexRs.next()) {
            String indexName = indexRs.getString("INDEX_NAME");
            if (indexName == null) continue;

            boolean unique = !indexRs.getBoolean("NON_UNIQUE");
            String columnName = indexRs.getString("COLUMN_NAME");
            int ordinalPosition = indexRs.getInt("ORDINAL_POSITION");

            indexes.add(new TableDetails.IndexInfo(indexName, unique, columnName, ordinalPosition));
        }
        indexRs.close();

        return indexes;
    }

    private List<TableDetails.ForeignKeyInfo> getForeignKeyInfo(
            DatabaseMetaData metaData, String catalog, String schema, String tableName) throws SQLException {

        List<TableDetails.ForeignKeyInfo> foreignKeys = new ArrayList<>();
        ResultSet fkRs = metaData.getImportedKeys(catalog, schema, tableName);

        while (fkRs.next()) {
            String fkName = fkRs.getString("FK_NAME");
            String fkColumn = fkRs.getString("FKCOLUMN_NAME");
            String pkTableName = fkRs.getString("PKTABLE_NAME");
            String pkColumn = fkRs.getString("PKCOLUMN_NAME");
            
            int updateRuleCode = fkRs.getInt("UPDATE_RULE");
            int deleteRuleCode = fkRs.getInt("DELETE_RULE");
            
            String updateRule = getRuleName(updateRuleCode);
            String deleteRule = getRuleName(deleteRuleCode);

            foreignKeys.add(new TableDetails.ForeignKeyInfo(
                    fkName, fkColumn, pkTableName, pkColumn, updateRule, deleteRule));
        }
        fkRs.close();

        return foreignKeys;
    }

    private String getRuleName(int ruleCode) {
        return switch (ruleCode) {
            case DatabaseMetaData.importedKeyCascade -> "CASCADE";
            case DatabaseMetaData.importedKeyRestrict -> "RESTRICT";
            case DatabaseMetaData.importedKeySetNull -> "SET NULL";
            case DatabaseMetaData.importedKeySetDefault -> "SET DEFAULT";
            case DatabaseMetaData.importedKeyNoAction -> "NO ACTION";
            default -> "UNKNOWN";
        };
    }
}
