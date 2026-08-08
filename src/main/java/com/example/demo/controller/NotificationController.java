package com.example.demo.controller;

import com.example.demo.dto.NotificationDto;
import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication authentication) {
        Integer userId = getUserId(authentication);
        List<NotificationDto> dtos = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Integer userId = getUserId(authentication);
        notificationRepository.findById(id).ifPresent(notification -> {
            if (notification.getUser().getUserId().equals(userId)) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
        return ResponseEntity.ok().build();
    }

    private NotificationDto mapToDto(Notification notif) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notif.getId());
        dto.setMessage(notif.getMessage());
        dto.setIsRead(notif.getIsRead());
        dto.setCreatedAt(notif.getCreatedAt());
        return dto;
    }
}
