package com.example.demo.controller;

import com.example.demo.entity.Banner;
import com.example.demo.repository.BannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
public class AdminBannerController {

    private final BannerRepository bannerRepository;

    public AdminBannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerRepository.findAll());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
        return ResponseEntity.ok(bannerRepository.save(banner));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Banner> updateBanner(@PathVariable Integer id, @RequestBody Banner request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));
        
        banner.setTitle(request.getTitle());
        banner.setSubtitle(request.getSubtitle());
        banner.setImageUrl(request.getImageUrl());
        banner.setLinkUrl(request.getLinkUrl());
        banner.setActive(request.isActive());
        banner.setSortOrder(request.getSortOrder());
        
        return ResponseEntity.ok(bannerRepository.save(banner));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteBanner(@PathVariable Integer id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
