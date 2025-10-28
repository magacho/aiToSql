package com.magacho.aiToSql.dto;

import java.util.List;
import java.util.Map;

public record QueryResult(
        String query,
        int rowCount,
        int maxRowsReached,
        List<String> columnNames,
        List<Map<String, Object>> data
) {
}
