package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_tenants",
       indexes = @Index(name = "idx_user_tenants_email", columnList = "email"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String tenantId;

}