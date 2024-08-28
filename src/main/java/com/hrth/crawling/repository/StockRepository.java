package com.hrth.crawling.repository;

import com.hrth.crawling.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {
}
