package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "concerns")
@Getter
@Setter
@NoArgsConstructor
public class SkinConcern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concern_id")
    private Long concernId;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "concerns")
    private List<Product> products;
}
