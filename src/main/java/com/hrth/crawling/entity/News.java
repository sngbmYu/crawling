package com.hrth.crawling.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_code")
    private Stock stock;

    @Setter
    @Column(name = "news_date")
    private String date;

    private String title;

    @Setter
    private String url;

    private String publisher;
}
