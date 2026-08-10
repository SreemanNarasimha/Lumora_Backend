package com.example.demo.controller;

import com.example.demo.dto.SkinConcernDto;
import com.example.demo.entity.SkinConcern;
import com.example.demo.repository.SkinConcernRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/concerns")
public class SkinConcernController {

    private final SkinConcernRepository concernRepository;

    public SkinConcernController(SkinConcernRepository concernRepository) {
        this.concernRepository = concernRepository;
    }

    @GetMapping
    public ResponseEntity<List<SkinConcernDto>> getConcerns() {
        List<SkinConcernDto> concerns = concernRepository.findAll().stream().map(c -> {
            SkinConcernDto dto = new SkinConcernDto();
            dto.setId(c.getConcernId());
            dto.setLabel(c.getName());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(concerns);
    }
}
