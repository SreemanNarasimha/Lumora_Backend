package com.example.demo.dto;

import lombok.Data;

@Data
public class JournalEntryRequest {
    private String content;
    private String mood;
    private Integer promptId;
}
