package com.example.demo.controller;

import com.example.demo.dto.JournalEntryDto;
import com.example.demo.dto.JournalEntryRequest;
import com.example.demo.dto.JournalPromptDto;
import com.example.demo.entity.JournalEntry;
import com.example.demo.entity.JournalPrompt;
import com.example.demo.entity.User;
import com.example.demo.repository.JournalEntryRepository;
import com.example.demo.repository.JournalPromptRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private JournalPromptRepository journalPromptRepository;

    @Autowired
    private UserRepository userRepository;
    
    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    // Get all journal entries for a user
    @GetMapping("/entries")
    public ResponseEntity<List<JournalEntryDto>> getUserEntries(Authentication authentication) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        List<JournalEntry> entries = journalEntryRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        List<JournalEntryDto> dtos = entries.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Create a new journal entry
    @PostMapping("/entries")
    public ResponseEntity<JournalEntryDto> createEntry(
            Authentication authentication,
            @RequestBody JournalEntryRequest request) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        JournalEntry entry = new JournalEntry();
        entry.setUser(user);
        entry.setContent(request.getContent());
        entry.setMood(request.getMood());

        if (request.getPromptId() != null) {
            JournalPrompt prompt = journalPromptRepository.findById(request.getPromptId()).orElse(null);
            entry.setPrompt(prompt);
        }

        JournalEntry savedEntry = journalEntryRepository.save(entry);
        return ResponseEntity.ok(mapToDto(savedEntry));
    }

    // Delete a journal entry
    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> deleteEntry(
            Authentication authentication,
            @PathVariable Integer id) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        JournalEntry entry = journalEntryRepository.findById(id).orElse(null);
        if (entry == null) return ResponseEntity.notFound().build();
        
        if (!entry.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        journalEntryRepository.delete(entry);
        return ResponseEntity.ok().build();
    }

    // Get a random prompt
    @GetMapping("/prompts/random")
    public ResponseEntity<JournalPromptDto> getRandomPrompt() {
        JournalPrompt prompt = journalPromptRepository.findRandomPrompt();
        if (prompt == null) return ResponseEntity.notFound().build();

        JournalPromptDto dto = new JournalPromptDto();
        dto.setId(prompt.getId());
        dto.setText(prompt.getText());
        dto.setCategory(prompt.getCategory());
        return ResponseEntity.ok(dto);
    }

    // --- Helper Methods ---

    private JournalEntryDto mapToDto(JournalEntry entry) {
        JournalEntryDto dto = new JournalEntryDto();
        dto.setId(entry.getId());
        dto.setContent(entry.getContent());
        dto.setMood(entry.getMood());
        if (entry.getPrompt() != null) {
            dto.setPromptId(entry.getPrompt().getId());
        }
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        return dto;
    }
}
