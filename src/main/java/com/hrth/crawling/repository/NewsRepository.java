package com.hrth.crawling.repository;

import com.hrth.crawling.entity.News;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NewsRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchInsert(List<News> newsList) {
        String sql = "INSERT INTO news" +
                " (news_date, title, url, stock_code, publisher)" +
                " VALUES (?,?,?,?,?)";

        jdbcTemplate.batchUpdate(sql, newsList, newsList.size(), (ps, news) -> {
            ps.setString(1, news.getDate());
            ps.setString(2, news.getTitle());
            ps.setString(3, news.getUrl());
            ps.setString(4, news.getStock().getCode());
            ps.setString(5, news.getPublisher());
        });
    }

    public String findEarliestDateByStockCode(String stockCode) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("00yyyyMMdd");

        String sql = "SELECT DATE_SUB(MIN(STR_TO_DATE(news_date, '%Y/%m/%d')), INTERVAL 1 DAY) as earliest_date " +
                "FROM news WHERE stock_code = ?";

        Date date = jdbcTemplate.queryForObject(sql, new Object[]{stockCode}, Date.class);

        return dateFormat.format(date);
    }

    public String findLatestDateByStockCode(String stockCode) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        String sql = "SELECT MAX(STR_TO_DATE(news_date, '%Y/%m/%d')) as latest_date " +
                "FROM news WHERE stock_code = ?";

        Date date = jdbcTemplate.queryForObject(sql, new Object[]{stockCode}, Date.class);

        return dateFormat.format(date);
    }
}
