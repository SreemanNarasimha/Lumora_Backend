package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rituals")
@Getter
@Setter
@NoArgsConstructor
public class Ritual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String category; // e.g. "Morning", "Evening"

    private String description;

    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false;

    @Column(name = "owner_id")
    private Integer ownerId; // null for global pre-built rituals

    @OneToMany(mappedBy = "ritual", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<RitualStep> steps;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
