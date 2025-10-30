package com.hermes.userservice.repository;

import com.hermes.userservice.entity.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, Long> {

    Optional<UserTenant> findByEmail(String email);
}