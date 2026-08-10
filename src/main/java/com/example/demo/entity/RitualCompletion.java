package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ritual_completions")
@Getter
@Setter
@NoArgsConstructor
public class RitualCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ritual_id", nullable = false)
    private Integer ritualId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;

    private String notes;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }
}
