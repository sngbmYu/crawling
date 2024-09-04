package com.hrth.crawling.service;

import com.hrth.crawling.entity.News;
import com.hrth.crawling.entity.Stock;
import com.hrth.crawling.repository.NewsRepository;
import com.hrth.crawling.repository.StockRepository;
import com.hrth.crawling.util.KisApiAuthManager;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingService {
    private static final int DELAY_TIME = 1500;
    private static final int MIN_DUPLICATE_MORPHEME_COUNT = 5;
    private static final String STOCK_CODE_CONDITION = "000650";


    private final WebDriver webDriver;
    private final StockRepository stockRepository;
    private final NewsRepository newsRepository;
    private final KisApiAuthManager kisApiAuthManager;

    public void performSearch() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
//            if (stock.getCode().compareToIgnoreCase(STOCK_CODE_CONDITION) <= 0) continue;

            List<News> newsList = new LinkedList<>();

            String startDate = newsRepository.findEarliestDateByStockCode(stock.getCode());
            while (startDate != null && startDate.compareToIgnoreCase("0020240701") >= 0) {
//                if (newsList.isEmpty()) {
//                    startDate = "0020240828";
//                } else {
//                    String earliest = newsList.get(newsList.size() - 1).getDate();
//
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//                    LocalDate localDate = LocalDate.parse(earliest, formatter).minusDays(1);
//                    DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("00yyyyMMdd");
//
//                    startDate = localDate.format(newFormatter);
//                }

                List<News> newNewsList = getNewsList(stock, startDate);
                newsList.addAll(newNewsList);
                log.info("news list size: {}", newsList.size());

                String earliest = newsList.get(newsList.size() - 1).getDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate localDate = LocalDate.parse(earliest, formatter).minusDays(1);
                DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("00yyyyMMdd");

                startDate = localDate.format(newFormatter);
            }

            if (newsList.isEmpty()) continue;

            dedupeNewsList(newsList);
            log.info("중복 제거 리스트 사이즈: {}", newsList.size());

            List<News> crawledNewsList = new ArrayList<>();
            for (News news : newsList) {
                crawlNews(news, crawledNewsList);
            }

            log.info("crawledNewsList size: {}", crawledNewsList.size());
            if (!crawledNewsList.isEmpty())
                newsRepository.batchInsert(crawledNewsList);
        }
    }

    private void dedupeNewsList(List<News> newsList) {
        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
        List<News> uniqueNewsList = new ArrayList<>();

        for (int i = 0; i < newsList.size(); i++) {
            News currentNews = newsList.get(i);

            String title = currentNews.getTitle();
            if (title == null || title.isEmpty()) continue;

            KomoranResult currentResult = komoran.analyze(title);
            List<String> currentMorphemes = getMorphemes(currentResult);

            boolean isDuplicate = false;
            for (int j = i + 1; j < newsList.size(); j++) {
                News otherNews = newsList.get(j);

                String otherNewsTitle = otherNews.getTitle();

                if (otherNewsTitle == null || otherNewsTitle.isEmpty()) continue;

                KomoranResult otherResult = komoran.analyze(otherNewsTitle);
                List<String> otherMorphemes = getMorphemes(otherResult);

                long commonMorphemeCount = currentMorphemes.stream()
                        .filter(otherMorphemes::contains)
                        .count();

                if (commonMorphemeCount >= MIN_DUPLICATE_MORPHEME_COUNT) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate) {
                uniqueNewsList.add(currentNews);
            }
        }

        newsList.clear();
        newsList.addAll(uniqueNewsList);
    }

    private List<String> getMorphemes(KomoranResult result) {
        return result.getTokenList().stream()
                .map(Token::getMorph)
                .filter(morph -> !morph.matches("\\d+"))
                .toList();
    }

    private void crawlNews(News news, List<News> crawledNewsList) {
        String query = news.getTitle() + " " + news.getPublisher();
        String searchURL = "https://www.google.com/search?q=" + query;
        webDriver.get(searchURL);

        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        WebElement element;
        try {
            element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[jsname='UWckNb']")));
        } catch (RuntimeException e) {
            return;
        }

        String href = element.getAttribute("href");
        news.setUrl(href);

        if (href.endsWith(".com/") || href.endsWith(".co.kr/")) return;

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate date = LocalDate.parse(news.getDate(), inputFormatter);

        String formattedDate = date.format(outputFormatter);
        news.setDate(formattedDate);

        log.info("crawling news: {}", news);
        crawledNewsList.add(news);
        delay();
    }

    private void delay() {
        try {
            Thread.sleep(DELAY_TIME);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private List<News> getNewsList(Stock stock, String startDate) {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .build();

        Map response = restClient.get()
                .uri(getNewsUri(stock.getCode(), startDate))
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.set("Authorization", "Bearer " + kisApiAuthManager.generateToken());
                    httpHeaders.set("appkey", kisApiAuthManager.getAppKey());
                    httpHeaders.set("appsecret", kisApiAuthManager.getAppSecret());
                    httpHeaders.set("tr_id", "FHKST01011800");
                    httpHeaders.set("custtype", "P");
                })
                .retrieve()
                .body(Map.class);

        List<Map> list = (List<Map>) response.get("output");
        List<News> newsList = new ArrayList<>();
        for (Map map : list) {
            String title = (String) map.get("hts_pbnt_titl_cntt");
            String publisher = (String) map.get("dorg");
            String date = (String) map.get("data_dt");

            newsList.add(
                    News.builder()
                            .title(title)
                            .publisher(publisher)
                            .date(date)
                            .stock(stock)
                            .build()
            );
        }

        return newsList;
    }

    private String getNewsUri(String code, String startDate) {
        String baseUri = "/uapi/domestic-stock/v1/quotations/news-title";
        String params = "?FID_NEWS_OFER_ENTP_CODE=" +
                "&FID_COND_MRKT_CLS_CODE=" +
                "&FID_INPUT_ISCD=" + code +
                "&FID_TITL_CNTT=" +
//                "&FID_INPUT_DATE_1=" +
                "&FID_INPUT_DATE_1=" + startDate +
                "&FID_INPUT_HOUR_1=" +
                "&FID_RANK_SORT_CLS_CODE=" +
                "&FID_INPUT_SRNO=";

        return baseUri + params;
    }
}
