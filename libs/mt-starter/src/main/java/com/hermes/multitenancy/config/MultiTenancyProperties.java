package com.hermes.multitenancy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 멀티테넌시 설정 속성
 */
@Data
@ConfigurationProperties(prefix = "hermes.multitenancy")
public class MultiTenancyProperties {

    /**
     * 멀티테넌시 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * Flyway 설정
     */
    private FlywayConfig flyway = new FlywayConfig();

    /**
     * RabbitMQ 설정
     */
    private RabbitMQConfig rabbitmq = new RabbitMQConfig();

    @Data
    public static class FlywayConfig {

        /**
         * Flyway 멀티테넌시 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 애플리케이션 시작 시 기존 테넌트 스키마에 대한 자동 migration 실행 여부
         */
        private boolean startupMigrationEnabled = true;

        /**
         * 테넌트 스키마용 migration 위치
         */
        private List<String> locations = List.of("classpath:db/migration/tenant");

        /**
         * 기본 스키마용 migration 위치 (공통 테이블용)
         */
        private List<String> defaultLocations = List.of("classpath:db/migration");

        /**
         * migration 테이블명
         */
        private String table = "flyway_schema_history";

        /**
         * 베이스라인 버전
         */
        private String baselineVersion = "1";

        /**
         * 베이스라인 설명
         */
        private String baselineDescription = "Initial tenant schema";

        /**
         * 베이스라인 자동 생성 여부
         */
        private boolean baselineOnMigrate = true;

        /**
         * 스키마 검증 활성화 여부
         */
        private boolean validateOnMigrate = true;

        /**
         * 빈 스키마에서 migration 허용 여부
         */
        private boolean cleanOnValidationError = false;

        /**
         * migration 실행 시 트랜잭션 사용 여부
         */
        private boolean executeInTransaction = true;
    }

    @Data
    public static class RabbitMQConfig {

        /**
         * RabbitMQ 사용 여부
         */
        private boolean enabled = true;

        /**
         * 테넌트 이벤트 Exchange 이름
         */
        private String tenantExchange = "tenant.events";

        /**
         * 테넌트 이벤트 Queue 이름 패턴 ({serviceName}이 실제 서비스명으로 치환됨)
         */
        private String tenantQueuePattern = "tenant.events.{serviceName}";

        /**
         * 테넌트 생성 이벤트 라우팅 키
         */
        private String tenantCreatedRoutingKey = "tenant.created";

        /**
         * 테넌트 삭제 이벤트 라우팅 키
         */
        private String tenantDeletedRoutingKey = "tenant.deleted";

        /**
         * 테넌트 업데이트 이벤트 라우팅 키
         */
        private String tenantUpdatedRoutingKey = "tenant.updated";

        /**
         * 테넌트 상태 변경 이벤트 라우팅 키
         */
        private String tenantStatusChangedRoutingKey = "tenant.status.changed";

        /**
         * Dead Letter Exchange 이름
         */
        private String deadLetterExchange = "tenant.events.dlx";

        /**
         * Dead Letter Queue 이름 패턴
         */
        private String deadLetterQueuePattern = "tenant.events.dlq.{serviceName}";
    }

}
