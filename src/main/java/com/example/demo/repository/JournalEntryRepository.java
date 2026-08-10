package com.example.demo.repository;

import com.example.demo.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Integer> {
    List<JournalEntry> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
}
