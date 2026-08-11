package com.example.demo.controller;

import com.example.demo.entity.GlobalSetting;
import com.example.demo.repository.SettingRepository;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class AdminSettingsController {

    private final SettingRepository settingRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminSettingsController(SettingRepository settingRepository, AuditLogRepository auditLogRepository) {
        this.settingRepository = settingRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<GlobalSetting>> getSettings() {
        return ResponseEntity.ok(settingRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<GlobalSetting> updateSetting(@RequestBody GlobalSetting setting) {
        GlobalSetting saved = settingRepository.save(setting);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc());
    }
}
