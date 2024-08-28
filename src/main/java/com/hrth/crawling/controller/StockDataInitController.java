package com.hrth.crawling.controller;

import com.hrth.crawling.service.StockDataInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
public class StockDataInitController {
    private final StockDataInitService stockDataInitService;

    @GetMapping
    public ResponseEntity<Void> init() {
        try {
            stockDataInitService.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }
}
