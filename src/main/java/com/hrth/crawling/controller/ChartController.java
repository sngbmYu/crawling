package com.hrth.crawling.controller;

import com.hrth.crawling.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chart")
public class ChartController {
    private final ChartService chartService;

    @GetMapping
    public ResponseEntity<?> chart() {
        chartService.initChartDB();
        return ResponseEntity.ok().build();
    }
}
