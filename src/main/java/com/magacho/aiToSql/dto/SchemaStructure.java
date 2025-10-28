package com.magacho.aiToSql.dto;

import java.util.List;

public record SchemaStructure(
        String databaseName,
        String databaseType,
        List<TableInfo> tables
) {
    public record TableInfo(
            String tableName,
            String tableType,
            List<ColumnInfo> columns
    ) {
    }

    public record ColumnInfo(
            String columnName,
            String dataType,
            Integer columnSize,
            boolean nullable,
            boolean isPrimaryKey
    ) {
    }
}
