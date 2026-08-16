package com.example.demo.repository;
import com.example.demo.entity.SkinType;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SkinTypeRepository extends JpaRepository<SkinType, Long> {
    java.util.Optional<SkinType> findByName(String name);
}
