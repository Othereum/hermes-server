package com.hermes.multitenancy.config;

import com.hermes.multitenancy.flyway.FlywayTenantInitializer;
import com.hermes.multitenancy.flyway.TenantSchemaMigrationInitializer;
import com.hermes.multitenancy.hibernate.SchemaBasedConnectionProvider;
import com.hermes.multitenancy.hibernate.TenantIdentifierResolver;
import com.hermes.multitenancy.util.SchemaUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * 멀티테넌시 자동 설정
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({MultiTenancyProperties.class})
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.hermes.multitenancy")
public class MultiTenancyAutoConfiguration {

    @Bean
    @Primary
    public HibernatePropertiesCustomizer multiTenantHibernatePropertiesCustomizer(
            SchemaBasedConnectionProvider connectionProvider,
            TenantIdentifierResolver tenantResolver) {

        log.info("Configuring Hibernate multitenancy with SchemaBasedConnectionProvider and TenantIdentifierResolver");

        return (Map<String, Object> hibernateProperties) -> {
            hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
            hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);

            log.debug("Hibernate multitenancy properties configured: connection_provider={}, identifier_resolver={}",
                connectionProvider.getClass().getSimpleName(), tenantResolver.getClass().getSimpleName());
        };
    }

    /**
     * 애플리케이션 시작 시 테넌트 스키마 자동 Migration 초기화
     */
    @Bean
    @ConditionalOnProperty(name = "hermes.multitenancy.flyway.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnProperty(name = "hermes.multitenancy.flyway.startupMigrationEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "org.flywaydb.core.Flyway")
    public TenantSchemaMigrationInitializer tenantSchemaMigrationInitializer(
            SchemaUtils schemaUtils,
            FlywayTenantInitializer flywayTenantInitializer,
            MultiTenancyProperties multiTenancyProperties) {

        log.info("Registering TenantSchemaMigrationInitializer for automatic startup migration");
        return new TenantSchemaMigrationInitializer(schemaUtils, flywayTenantInitializer, multiTenancyProperties);
    }
}
