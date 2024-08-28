package com.hrth.crawling.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chart_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_code")
    private Stock stock;

    private int open;

    private int close;

    private int high;

    private int low;

    @Column(name = "chart_date")
    private String date;
}