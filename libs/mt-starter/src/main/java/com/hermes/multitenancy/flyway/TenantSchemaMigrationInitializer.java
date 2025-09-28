package com.hermes.multitenancy.flyway;

import com.hermes.multitenancy.config.MultiTenancyProperties;
import com.hermes.multitenancy.util.SchemaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * 애플리케이션 시작 시 기존 테넌트 스키마들에 대한 자동 Migration 실행기
 *
 * 이 컴포넌트는 애플리케이션이 시작될 때 데이터베이스에서 "tenant_"로 시작하는
 * 기존 스키마들을 자동으로 찾아서 Flyway migration을 실행합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Order(1000) // 다른 초기화 작업 이후에 실행
public class TenantSchemaMigrationInitializer implements ApplicationRunner {

    private final SchemaUtils schemaUtils;
    private final FlywayTenantInitializer flywayTenantInitializer;
    private final MultiTenancyProperties multiTenancyProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!multiTenancyProperties.getFlyway().isEnabled()) {
            log.info("Flyway is disabled, skipping tenant schema migration initialization");
            return;
        }

        if (!multiTenancyProperties.getFlyway().isStartupMigrationEnabled()) {
            log.info("Startup migration is disabled, skipping tenant schema migration initialization");
            return;
        }

        log.info("Starting tenant schema migration initialization...");

        try {
            // 기존 tenant 스키마들 조회
            List<String> tenantSchemas = schemaUtils.getAllTenantSchemas();

            if (tenantSchemas.isEmpty()) {
                log.info("No existing tenant schemas found");
                return;
            }

            log.info("Found {} existing tenant schemas", tenantSchemas.size());

            // 각 스키마의 migration 상태 확인 및 로그
            logMigrationStatus(tenantSchemas);

            // 일괄 migration 실행
            flywayTenantInitializer.runMigrationsForSchemas(tenantSchemas);

            log.info("Tenant schema migration initialization completed successfully");

        } catch (Exception e) {
            log.error("Failed to initialize tenant schema migrations", e);

            // 실패해도 애플리케이션 시작을 중단하지 않음
            // 필요시 이 부분을 수정하여 실패 시 애플리케이션을 중단할 수 있음
            log.warn("Application will continue despite migration initialization failure");
        }
    }

    /**
     * 각 스키마의 migration 상태를 로깅
     */
    private void logMigrationStatus(List<String> tenantSchemas) {
        log.info("Checking migration status for {} tenant schemas...", tenantSchemas.size());

        for (String schemaName : tenantSchemas) {
            try {
                boolean migrationRequired = flywayTenantInitializer.isMigrationRequired(schemaName);
                if (migrationRequired) {
                    log.info("Schema '{}' has pending migrations", schemaName);
                    flywayTenantInitializer.logMigrationInfo(schemaName);
                } else {
                    log.debug("Schema '{}' is up to date", schemaName);
                }
            } catch (Exception e) {
                log.warn("Failed to check migration status for schema: {}", schemaName, e);
            }
        }
    }
}