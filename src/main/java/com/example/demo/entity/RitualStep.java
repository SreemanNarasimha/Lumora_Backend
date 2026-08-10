package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ritual_steps")
@Getter
@Setter
@NoArgsConstructor
public class RitualStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ritual_id", nullable = false)
    @JsonIgnore
    private Ritual ritual;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(nullable = false, length = 500)
    private String instruction;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
