package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "content_items")
@Getter
@Setter
@NoArgsConstructor
public class ContentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type; // article, guided_meditation, practice

    private String category;

    @ElementCollection
    @CollectionTable(name = "content_tags", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    private String author;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
}
