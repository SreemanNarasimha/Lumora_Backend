package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
