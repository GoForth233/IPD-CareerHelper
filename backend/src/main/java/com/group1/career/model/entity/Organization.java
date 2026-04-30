package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A B-side customer (university department, training cohort, employer
 * recruiting team). Sprint D-4 introduced the org concept so that the
 * admin console can pivot dashboards by ownership without reusing the
 * Role table for tenancy.
 *
 * <p>{@code users.org_id} is a soft FK — we don't enforce it at the DB
 * level so the existing C-side users without an org keep working.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organizations", indexes = {
        @Index(name = "idx_org_code", columnList = "code", unique = true)
})
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "org_id")
    private Long orgId;

    /** Short slug used in admin URLs and emails. */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "contact_name", length = 80)
    private String contactName;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
