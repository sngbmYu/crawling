package com.hrth.crawling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "stocks")
public class Stock {
    @Id
    @Column(name = "stock_code")
    private String code;

    @Column(name = "stock_name")
    private String name;

    private String logo;
}
