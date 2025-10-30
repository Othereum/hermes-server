package com.hermes.multitenancy.flyway;

import com.hermes.multitenancy.config.MultiTenancyProperties;
import com.hermes.multitenancy.util.SchemaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 테넌트별 Flyway Migration 실행기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlywayTenantInitializer {
    
    private final DataSource dataSource;
    private final SchemaUtils schemaUtils;
    private final MultiTenancyProperties multiTenancyProperties;
    
    /**
     * 새 테넌트 스키마 생성 및 Migration 실행
     */
    public void initializeTenantSchema(String tenantId, String schemaName) {
        try {
            log.info("Initializing tenant schema: {} for tenant: {}", schemaName, tenantId);
            
            // 1. 스키마 생성
            schemaUtils.createSchema(schemaName);
            log.info("Schema created: {}", schemaName);
            
            // 2. Flyway migration 실행
            if (multiTenancyProperties.getFlyway().isEnabled()) {
                runFlywayMigration(schemaName);
                log.info("Flyway migration completed for schema: {}", schemaName);
            } else {
                log.info("Flyway is disabled, skipping migration for schema: {}", schemaName);
            }
            
        } catch (Exception e) {
            log.error("Failed to initialize tenant schema: {} for tenant: {}", schemaName, tenantId, e);
            throw new RuntimeException("Tenant schema initialization failed", e);
        }
    }
    
    /**
     * 테넌트 스키마 삭제
     */
    public void dropTenantSchema(String tenantId, String schemaName) {
        try {
            log.info("Dropping tenant schema: {} for tenant: {}", schemaName, tenantId);
            
            // 스키마 삭제
            schemaUtils.dropSchema(schemaName);
            log.info("Schema dropped: {}", schemaName);
            
        } catch (Exception e) {
            log.error("Failed to drop tenant schema: {} for tenant: {}", schemaName, tenantId, e);
            throw new RuntimeException("Tenant schema deletion failed", e);
        }
    }
    
    /**
     * 특정 스키마에 대해 Flyway Migration 실행
     */
    private void runFlywayMigration(String schemaName) {
        log.debug("Running Flyway migration for schema: {}", schemaName);
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)  // 특정 스키마 대상
                .locations(multiTenancyProperties.getFlyway().getLocations().toArray(new String[0]))
                .table(multiTenancyProperties.getFlyway().getTable())
                .baselineVersion(multiTenancyProperties.getFlyway().getBaselineVersion())
                .baselineDescription(multiTenancyProperties.getFlyway().getBaselineDescription())
                .baselineOnMigrate(multiTenancyProperties.getFlyway().isBaselineOnMigrate())
                .validateOnMigrate(multiTenancyProperties.getFlyway().isValidateOnMigrate())
                .cleanOnValidationError(multiTenancyProperties.getFlyway().isCleanOnValidationError())
                .executeInTransaction(multiTenancyProperties.getFlyway().isExecuteInTransaction())
                .load();
        
        // Migration 실행
        int migrationsExecuted = flyway.migrate().migrationsExecuted;
        log.info("Executed {} migrations for schema: {}", migrationsExecuted, schemaName);
    }
    
    /**
     * 스키마의 Migration 상태 확인
     */
    public boolean isMigrationRequired(String schemaName) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations(multiTenancyProperties.getFlyway().getLocations().toArray(new String[0]))
                    .table(multiTenancyProperties.getFlyway().getTable())
                    .load();
                    
            return flyway.info().pending().length > 0;
            
        } catch (Exception e) {
            log.warn("Could not check migration status for schema: {}", schemaName, e);
            return true; // 확인할 수 없으면 migration 필요하다고 가정
        }
    }
    
    /**
     * 스키마 Migration 상태 정보 로깅
     */
    public void logMigrationInfo(String schemaName) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations(multiTenancyProperties.getFlyway().getLocations().toArray(new String[0]))
                    .table(multiTenancyProperties.getFlyway().getTable())
                    .load();

            var info = flyway.info();
            log.info("Migration info for schema '{}': {} applied, {} pending",
                    schemaName, info.applied().length, info.pending().length);

        } catch (Exception e) {
            log.warn("Could not retrieve migration info for schema: {}", schemaName, e);
        }
    }

    /**
     * 기존 스키마에 대해서만 Flyway Migration 실행 (스키마 생성 없이)
     */
    public void runMigrationOnly(String schemaName) {
        try {
            log.info("Running migration for existing schema: {}", schemaName);

            // 스키마 존재 여부 확인
            if (!schemaUtils.schemaExists(schemaName)) {
                log.warn("Schema does not exist, skipping migration: {}", schemaName);
                return;
            }

            // Flyway migration만 실행
            if (multiTenancyProperties.getFlyway().isEnabled()) {
                runFlywayMigration(schemaName);
                log.info("Flyway migration completed for existing schema: {}", schemaName);
            } else {
                log.info("Flyway is disabled, skipping migration for schema: {}", schemaName);
            }

        } catch (Exception e) {
            log.error("Failed to run migration for schema: {}", schemaName, e);
            throw new RuntimeException("Migration failed for schema: " + schemaName, e);
        }
    }

    /**
     * 여러 스키마에 대해 일괄적으로 Migration 실행
     */
    public void runMigrationsForSchemas(java.util.List<String> schemaNames) {
        if (schemaNames == null || schemaNames.isEmpty()) {
            log.info("No schemas provided for migration");
            return;
        }

        log.info("Starting migrations for {} schemas: {}", schemaNames.size(), schemaNames);

        int successCount = 0;
        int failureCount = 0;

        for (String schemaName : schemaNames) {
            try {
                runMigrationOnly(schemaName);
                successCount++;
            } catch (Exception e) {
                log.error("Migration failed for schema: {}", schemaName, e);
                failureCount++;
            }
        }

        log.info("Migration completed. Success: {}, Failed: {}", successCount, failureCount);

        if (failureCount > 0) {
            log.warn("Some migrations failed. Please check the logs for details.");
        }
    }
}
