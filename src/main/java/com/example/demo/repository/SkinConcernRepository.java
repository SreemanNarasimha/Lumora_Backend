package com.example.demo.repository;

import com.example.demo.entity.SkinConcern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkinConcernRepository extends JpaRepository<SkinConcern, Long> {
    Optional<SkinConcern> findByName(String name);
}
