package com.example.demo.repository;

import com.example.demo.entity.JournalPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalPromptRepository extends JpaRepository<JournalPrompt, Integer> {
    @Query(value = "SELECT * FROM journal_prompts ORDER BY RAND() LIMIT 1", nativeQuery = true)
    JournalPrompt findRandomPrompt();
}
