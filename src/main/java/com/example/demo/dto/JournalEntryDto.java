package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JournalEntryDto {
    private Integer id;
    private String content;
    private String mood;
    private Integer promptId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
