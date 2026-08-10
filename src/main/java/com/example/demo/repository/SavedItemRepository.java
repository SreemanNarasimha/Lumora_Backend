package com.example.demo.repository;

import com.example.demo.entity.SavedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, Integer> {
    List<SavedItem> findByUserId(Integer userId);
    Optional<SavedItem> findByUserIdAndContentItemId(Integer userId, Integer contentItemId);
}
