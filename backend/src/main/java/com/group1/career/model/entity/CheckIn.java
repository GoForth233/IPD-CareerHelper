package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One row per (user, action) per calendar day. Lets us answer:
 *   - How many distinct days has this user shown up this week?
 *   - Did they hit all three "core actions" today? (assessment + interview + skill node)
 *   - What's their current streak?
 *
 * Why a row per (user, action, day) instead of bumping a counter?
 * Because we want the streak to require *real* progress on at least one
 * meaningful action. Multiple submissions of the same action in one day
 * collapse to a single row by the unique key.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "check_ins", uniqueConstraints = {
        @UniqueConstraint(name = "uniq_user_day_action",
                columnNames = {"user_id", "check_day", "action"})
}, indexes = {
        @Index(name = "idx_check_ins_user_day", columnList = "user_id, check_day")
})
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Calendar date in the server's timezone (Asia/Shanghai). Stored as
     *  {@code check_day} because some DB engines (H2) reserve {@code DAY}
     *  as a keyword and reject it as a column name. */
    @Column(name = "check_day", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate day;

    /** ASSESSMENT, INTERVIEW, SKILL_NODE. */
    @Column(name = "action", length = 32, nullable = false)
    private String action;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
