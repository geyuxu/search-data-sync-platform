package com.example.searchmiddleware.controller;

import com.example.searchmiddleware.service.ArticleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final ArticleSyncService articleSyncService;

    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        try {
            String result = articleSyncService.rebuildIndex();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Rebuild failed: " + e.getMessage());
        }
    }
}
