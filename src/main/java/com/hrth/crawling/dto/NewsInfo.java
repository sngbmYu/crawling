package com.hrth.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsInfo {
    private String publisher;
    private String title;
    private String company;
    private String date;
}
