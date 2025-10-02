package com.hermes.multitenancy.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 테넌트 컨텍스트 관리 클래스
 * ThreadLocal을 사용하여 현재 요청의 테넌트 정보를 관리
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> tenantIdHolder = new ThreadLocal<>();

    /**
     * 현재 스레드에 테넌트 ID 설정
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("Attempting to set null or empty tenant ID");
            return;
        }

        log.debug("Setting tenant context: {}", tenantId);
        tenantIdHolder.set(tenantId);
    }

    /**
     * 현재 테넌트 ID 반환
     */
    public static String getCurrentTenantId() {
        String tenantId = tenantIdHolder.get();
        if (tenantId == null) {
            throw new IllegalStateException("테넌트 컨텍스트가 설정되지 않았습니다");
        }

        return tenantId;
    }

    /**
     * 테넌트 컨텍스트가 설정되어 있는지 확인
     */
    public static boolean hasTenantContext() {
        return tenantIdHolder.get() != null;
    }

    /**
     * 테넌트 컨텍스트 정리
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        tenantIdHolder.remove();
    }

    /**
     * 테넌트 설정과 함께 작업 실행
     *
     * <p><strong>중요:</strong> 이 메서드는 활성 트랜잭션과 DB 커넥션이 없는 상태에서 호출되어야 합니다.
     * 트랜잭션이나 커넥션이 먼저 획득되면 멀티테넌시가 제대로 작동하지 않습니다.</p>
     *
     * <p><strong>필수 설정:</strong></p>
     * <pre>
     * spring.jpa.open-in-view=false
     * </pre>
     * <p>이 설정이 true이면 요청 시작 시점에 커넥션이 획득되어 테넌트 컨텍스트 변경이 불가능합니다.</p>
     *
     * <p><strong>올바른 사용법:</strong></p>
     * <pre>{@code
     * // 1. NonTenant 컨텍스트에서 테넌트 조회
     * String tenantId = getTenantId(email);
     *
     * // 2. 테넌트 컨텍스트 설정 후 @Transactional 메서드 호출
     * return TenantContext.executeWithTenant(tenantId, () -> {
     *     return transactionalService.doSomething(); // 여기서 트랜잭션 시작
     * });
     * }</pre>
     *
     * <p><strong>잘못된 사용법:</strong></p>
     * <pre>{@code
     * @Transactional // ← 여기서 트랜잭션 시작 (잘못됨)
     * public void method() {
     *     TenantContext.executeWithTenant(tenantId, () -> {
     *         // 이미 늦음 - 잘못된 테넌트로 커넥션 획득됨 (IllegalStateException 발생)
     *     });
     * }
     * }</pre>
     *
     * @param tenantId 설정할 테넌트 ID
     * @param operation 테넌트 컨텍스트에서 실행할 작업
     * @param <T> 작업 결과 타입
     * @return 작업 실행 결과
     * @throws IllegalStateException 현재 활성 트랜잭션이나 DB 커넥션이 있는 경우
     */
    public static <T> T executeWithTenant(String tenantId, TenantOperation<T> operation) {
        // 1. 활성 트랜잭션 확인
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(
                "executeWithTenant()은 활성 트랜잭션이 없는 상태에서 호출되어야 합니다. " +
                "트랜잭션이 시작되기 전에 테넌트 컨텍스트를 설정해야 합니다."
            );
        }

        // 2. 바인딩된 리소스(커넥션 등) 확인
        if (!TransactionSynchronizationManager.getResourceMap().isEmpty()) {
            throw new IllegalStateException(
                "executeWithTenant()은 활성 DB 커넥션이 없는 상태에서 호출되어야 합니다. " +
                "이미 커넥션이 획득된 후에는 테넌트 컨텍스트를 변경할 수 없습니다. " +
                "spring.jpa.open-in-view 설정을 확인해보세요. false로 설정되어야 합니다."
            );
        }

        String previousTenantId = tenantIdHolder.get();

        try {
            setTenantId(tenantId);
            return operation.execute();
        } finally {
            if (previousTenantId != null) {
                setTenantId(previousTenantId);
            } else {
                clear();
            }
        }
    }

    /**
     * 테넌트 컨텍스트에서 실행할 작업의 인터페이스
     */
    @FunctionalInterface
    public interface TenantOperation<T> {
        T execute();
    }
}
