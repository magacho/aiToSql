package com.magacho.aiToSql.service;

import com.magacho.aiToSql.dto.TriggerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Trigger Service
 * Provides information about database triggers for specific tables
 */
@Service
public class TriggerService {

    private static final Logger log = LoggerFactory.getLogger(TriggerService.class);
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public TriggerService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * List all triggers for a specific table
     * Implementation varies by database type (Oracle, MySQL, PostgreSQL, MSSQL)
     */
    @Cacheable("triggers")
    public TriggerList listTriggers(String tableName) throws SQLException {
        log.info("Retrieving triggers for table: {}", tableName);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseType = metaData.getDatabaseProductName().toUpperCase();

            List<TriggerList.TriggerInfo> triggers;
            if (databaseType.contains("POSTGRESQL")) {
                triggers = getPostgreSQLTriggers(tableName);
            } else if (databaseType.contains("MYSQL")) {
                triggers = getMySQLTriggers(tableName);
            } else if (databaseType.contains("ORACLE")) {
                triggers = getOracleTriggers(tableName);
            } else if (databaseType.contains("SQL SERVER")) {
                triggers = getMSSQLTriggers(tableName);
            } else {
                log.warn("Trigger listing not implemented for database type: {}", databaseType);
                triggers = new ArrayList<>();
            }

            log.info("Found {} triggers for table: {}", triggers.size(), tableName);
            return new TriggerList(tableName, triggers);
        }
    }

    private List<TriggerList.TriggerInfo> getPostgreSQLTriggers(String tableName) {
        String query = """
                SELECT trigger_name, event_manipulation, action_timing, action_statement
                FROM information_schema.triggers
                WHERE event_object_table = ?
                ORDER BY trigger_name
                """;

        return jdbcTemplate.query(query, new Object[]{tableName}, (rs, rowNum) ->
                new TriggerList.TriggerInfo(
                        rs.getString("trigger_name"),
                        rs.getString("event_manipulation"),
                        rs.getString("action_timing"),
                        rs.getString("action_statement")
                )
        );
    }

    private List<TriggerList.TriggerInfo> getMySQLTriggers(String tableName) {
        String query = """
                SELECT TRIGGER_NAME, EVENT_MANIPULATION, ACTION_TIMING, ACTION_STATEMENT
                FROM information_schema.TRIGGERS
                WHERE EVENT_OBJECT_TABLE = ?
                ORDER BY TRIGGER_NAME
                """;

        return jdbcTemplate.query(query, new Object[]{tableName}, (rs, rowNum) ->
                new TriggerList.TriggerInfo(
                        rs.getString("TRIGGER_NAME"),
                        rs.getString("EVENT_MANIPULATION"),
                        rs.getString("ACTION_TIMING"),
                        rs.getString("ACTION_STATEMENT")
                )
        );
    }

    private List<TriggerList.TriggerInfo> getOracleTriggers(String tableName) {
        String query = """
                SELECT trigger_name, triggering_event, trigger_type, trigger_body
                FROM all_triggers
                WHERE table_name = UPPER(?)
                ORDER BY trigger_name
                """;

        return jdbcTemplate.query(query, new Object[]{tableName}, (rs, rowNum) ->
                new TriggerList.TriggerInfo(
                        rs.getString("trigger_name"),
                        rs.getString("triggering_event"),
                        rs.getString("trigger_type"),
                        rs.getString("trigger_body")
                )
        );
    }

    private List<TriggerList.TriggerInfo> getMSSQLTriggers(String tableName) {
        String query = """
                SELECT 
                    t.name AS trigger_name,
                    CASE WHEN te.type = 1 THEN 'INSERT'
                         WHEN te.type = 2 THEN 'UPDATE'
                         WHEN te.type = 3 THEN 'DELETE'
                         ELSE 'MULTIPLE' END AS event,
                    CASE WHEN t.is_instead_of_trigger = 1 THEN 'INSTEAD OF' ELSE 'AFTER' END AS timing,
                    m.definition AS statement
                FROM sys.triggers t
                INNER JOIN sys.trigger_events te ON t.object_id = te.object_id
                INNER JOIN sys.sql_modules m ON t.object_id = m.object_id
                INNER JOIN sys.tables tb ON t.parent_id = tb.object_id
                WHERE tb.name = ?
                ORDER BY t.name
                """;

        return jdbcTemplate.query(query, new Object[]{tableName}, (rs, rowNum) ->
                new TriggerList.TriggerInfo(
                        rs.getString("trigger_name"),
                        rs.getString("event"),
                        rs.getString("timing"),
                        rs.getString("statement")
                )
        );
    }
}
