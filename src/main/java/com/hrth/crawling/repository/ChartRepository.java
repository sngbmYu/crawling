package com.hrth.crawling.repository;

import com.hrth.crawling.entity.Chart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChartRepository extends JpaRepository<Chart, Long> {
    List<Chart> findChartsByDate(String date);
}
