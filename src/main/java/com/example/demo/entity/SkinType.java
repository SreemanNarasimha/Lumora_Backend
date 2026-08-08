package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "skin_types")
@Getter
@Setter
@NoArgsConstructor
public class SkinType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skin_type_id")
    private Long skinTypeId;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "skinTypes")
    private List<Product> products;
}
