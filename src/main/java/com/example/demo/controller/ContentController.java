package com.example.demo.controller;

import com.example.demo.entity.ContentItem;
import com.example.demo.entity.SavedItem;
import com.example.demo.repository.ContentItemRepository;
import com.example.demo.repository.SavedItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentItemRepository contentItemRepository;

    @Autowired
    private SavedItemRepository savedItemRepository;

    private Integer getUserId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer) {
            return (Integer) principal;
        }
        return null;
    }

    // Get all content items
    @GetMapping
    public ResponseEntity<List<ContentItem>> getAllContent() {
        return ResponseEntity.ok(contentItemRepository.findAll());
    }

    // Get a specific content item
    @GetMapping("/{id}")
    public ResponseEntity<ContentItem> getContentById(@PathVariable Integer id) {
        Optional<ContentItem> item = contentItemRepository.findById(id);
        return item.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Toggle save status
    @PostMapping("/{id}/save")
    public ResponseEntity<Void> toggleSaveStatus(Authentication authentication, @PathVariable Integer id) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        Optional<ContentItem> contentItem = contentItemRepository.findById(id);
        if (contentItem.isEmpty()) return ResponseEntity.notFound().build();

        Optional<SavedItem> existingSave = savedItemRepository.findByUserIdAndContentItemId(userId, id);
        
        if (existingSave.isPresent()) {
            savedItemRepository.delete(existingSave.get());
        } else {
            SavedItem savedItem = new SavedItem();
            savedItem.setUserId(userId);
            savedItem.setContentItemId(id);
            savedItemRepository.save(savedItem);
        }
        
        return ResponseEntity.ok().build();
    }

    // Get saved items for user
    @GetMapping("/saved")
    public ResponseEntity<List<ContentItem>> getSavedContent(Authentication authentication) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        List<SavedItem> savedItems = savedItemRepository.findByUserId(userId);
        
        List<ContentItem> items = savedItems.stream()
                .map(s -> contentItemRepository.findById(s.getContentItemId()).orElse(null))
                .filter(item -> item != null)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(items);
    }
}
