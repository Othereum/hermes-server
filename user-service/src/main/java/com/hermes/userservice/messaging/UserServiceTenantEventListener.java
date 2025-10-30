package com.hermes.userservice.messaging;

import com.hermes.events.tenant.TenantEvent;
import com.hermes.multitenancy.config.MultiTenancyProperties;
import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.flyway.FlywayTenantInitializer;
import com.hermes.multitenancy.messaging.FlywayTenantEventListener;
import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * User Service 전용 테넌트 이벤트 리스너
 * 테넌트 생성 시 스키마 생성 후 최초 관리자 계정을 자동으로 생성합니다.
 */
@Slf4j
@Component("tenantEventListener")
public class UserServiceTenantEventListener extends FlywayTenantEventListener {

    private final UserService userService;

    public UserServiceTenantEventListener(
            MultiTenancyProperties properties,
            FlywayTenantInitializer flywayTenantInitializer,
            UserService userService) {
        super(properties, flywayTenantInitializer, "user-service");
        this.userService = userService;
    }

    @Override
    protected void handleTenantCreated(TenantEvent event) {
        // 1. 먼저 스키마 생성 (부모 클래스의 로직 실행)
        super.handleTenantCreated(event);

        // 2. 관리자 계정 생성
        if (event.getAdminEmail() != null && event.getAdminPassword() != null) {
            try {
                log.info("Creating initial admin user for tenant: {}", event.getTenantId());

                // 관리자 계정 생성
                UserCreateDto adminDto = new UserCreateDto();
                adminDto.setName("관리자");
                adminDto.setEmail(event.getAdminEmail());
                adminDto.setPassword(event.getAdminPassword());
                adminDto.setIsAdmin(true);

                TenantContext.executeWithTenant(event.getTenantId(), () -> {
                    userService.createUser(adminDto);
                    return null;
                });

                log.info("Successfully created initial admin user for tenant: {} with email: {}",
                        event.getTenantId(), event.getAdminEmail());

            } catch (Exception e) {
                log.error("Failed to create initial admin user for tenant: {}", event.getTenantId(), e);
                throw new RuntimeException("Failed to create initial admin user", e);
            }
        } else {
            log.warn("Skipping admin user creation - missing email or password for tenant: {}",
                    event.getTenantId());
        }
    }
}
