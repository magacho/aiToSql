package com.magacho.aiToSql.dto;

import java.util.List;

public record TableDetails(
        String tableName,
        String tableType,
        List<ColumnDetail> columns,
        List<IndexInfo> indexes,
        List<ForeignKeyInfo> foreignKeys,
        List<ConstraintInfo> constraints
) {
    public record ColumnDetail(
            String columnName,
            String dataType,
            Integer columnSize,
            Integer decimalDigits,
            boolean nullable,
            String defaultValue,
            boolean isPrimaryKey,
            boolean isAutoIncrement
    ) {
    }

    public record IndexInfo(
            String indexName,
            boolean unique,
            String columnName,
            int ordinalPosition
    ) {
    }

    public record ForeignKeyInfo(
            String fkName,
            String fkColumn,
            String pkTableName,
            String pkColumn,
            String updateRule,
            String deleteRule
    ) {
    }

    public record ConstraintInfo(
            String constraintName,
            String constraintType,
            String columnName
    ) {
    }
}
