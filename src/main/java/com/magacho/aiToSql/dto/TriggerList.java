package com.magacho.aiToSql.dto;

import java.util.List;

public record TriggerList(
        String tableName,
        List<TriggerInfo> triggers
) {
    public record TriggerInfo(
            String triggerName,
            String event,
            String timing,
            String statement
    ) {
    }
}
