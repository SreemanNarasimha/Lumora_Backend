package com.example.demo.controller;

import com.example.demo.entity.Ritual;
import com.example.demo.entity.RitualCompletion;
import com.example.demo.repository.RitualCompletionRepository;
import com.example.demo.repository.RitualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rituals")
public class RitualController {

    @Autowired
    private RitualRepository ritualRepository;

    @Autowired
    private RitualCompletionRepository completionRepository;

    private Integer getUserId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer) {
            return (Integer) principal;
        }
        return null;
    }

    // Get all rituals available to the user (global + custom)
    @GetMapping
    public ResponseEntity<List<Ritual>> getRituals(Authentication authentication) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        List<Ritual> rituals = ritualRepository.findByOwnerIdOrIsCustomFalse(userId);
        return ResponseEntity.ok(rituals);
    }

    // Get a specific ritual
    @GetMapping("/{id}")
    public ResponseEntity<Ritual> getRitualById(Authentication authentication, @PathVariable Integer id) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        Optional<Ritual> ritual = ritualRepository.findById(id);
        if (ritual.isEmpty()) return ResponseEntity.notFound().build();

        // Check if user is allowed to see this ritual
        Ritual r = ritual.get();
        if (r.getIsCustom() && !r.getOwnerId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(r);
    }

    // Log a ritual completion
    @PostMapping("/{id}/complete")
    public ResponseEntity<RitualCompletion> completeRitual(Authentication authentication, @PathVariable Integer id, @RequestBody(required = false) String notes) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        RitualCompletion completion = new RitualCompletion();
        completion.setRitualId(id);
        completion.setUserId(userId);
        completion.setNotes(notes);

        RitualCompletion saved = completionRepository.save(completion);
        return ResponseEntity.ok(saved);
    }

    // Get user's completion history
    @GetMapping("/history")
    public ResponseEntity<List<RitualCompletion>> getHistory(Authentication authentication) {
        Integer userId = getUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        List<RitualCompletion> history = completionRepository.findByUserIdOrderByCompletedAtDesc(userId);
        return ResponseEntity.ok(history);
    }
}
