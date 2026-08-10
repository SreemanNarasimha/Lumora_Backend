package com.example.demo.repository;

import com.example.demo.entity.ContentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentItemRepository extends JpaRepository<ContentItem, Integer> {
}
