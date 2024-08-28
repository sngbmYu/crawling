package com.hrth.crawling.repository;

import com.hrth.crawling.entity.Chart;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchInsert(List<Chart> chartList) {
        String sql = "INSERT INTO chart" +
                " (high, low, open, close, chart_date, stock_code)" +
                " VALUES (?,?,?,?,?,?)";

        jdbcTemplate.batchUpdate(sql, chartList, chartList.size(), (ps, chart) -> {
            ps.setInt(1, chart.getHigh());
            ps.setInt(2, chart.getLow());
            ps.setInt(3, chart.getOpen());
            ps.setInt(4, chart.getClose());
            ps.setString(5, chart.getDate());
            ps.setString(6, chart.getStock().getCode());
        });
    }
}
