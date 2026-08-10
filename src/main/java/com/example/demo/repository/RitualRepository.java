package com.example.demo.repository;

import com.example.demo.entity.Ritual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RitualRepository extends JpaRepository<Ritual, Integer> {
    List<Ritual> findByOwnerIdOrIsCustomFalse(Integer ownerId);
}
