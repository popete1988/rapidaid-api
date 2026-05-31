package com.rapidaid.api.controller;

import com.rapidaid.api.model.FirstAidResponse;
import com.rapidaid.api.model.TipResponse;
import com.rapidaid.api.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/firstaid")
    public ResponseEntity<List<FirstAidResponse>> getFirstAid() {
        return ResponseEntity.ok(contentService.getFirstAid());
    }

    @GetMapping("/tips")
    public ResponseEntity<List<TipResponse>> getTips() {
        return ResponseEntity.ok(contentService.getTips());
    }
}
