package com.backend.backend_crud.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(nullable = false)
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType typeRole;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school; // null = role hệ thống
}