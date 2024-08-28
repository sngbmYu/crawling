package com.hrth.crawling.controller;

import com.hrth.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/crawl")
public class CrawlingController {
    private final CrawlingService crawlingService;

    @GetMapping
    public ResponseEntity<Void> searchNews() {
        crawlingService.performSearch();

        return ResponseEntity.ok().build();
    }
}
