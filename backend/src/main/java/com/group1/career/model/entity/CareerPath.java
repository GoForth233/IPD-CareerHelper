package com.group1.career.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "career_paths")
public class CareerPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "path_id")
    private Integer pathId;

    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}

