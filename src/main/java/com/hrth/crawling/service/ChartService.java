package com.hrth.crawling.service;

import com.hrth.crawling.entity.Chart;
import com.hrth.crawling.entity.Stock;
import com.hrth.crawling.repository.BatchRepository;
import com.hrth.crawling.repository.StockRepository;
import com.hrth.crawling.util.KisApiAuthManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChartService {
    public static final String DATE = "stck_bsop_date";
    public static final String OPEN = "stck_oprc";
    public static final String CLOSE = "stck_clpr";
    public static final String LOW = "stck_lwpr";
    public static final String HIGH = "stck_hgpr";
    public static final String STOCK_CODE_CONDITION = "002360";

    private final KisApiAuthManager authManager;
    private final StockRepository stockRepository;
    private final BatchRepository batchRepository;

    public void initChartDB() {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .build();

        String startDate = "20231025";
        String endDate = "20240201";

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
//            if (stock.getCode().compareToIgnoreCase(STOCK_CODE_CONDITION) <= 0) continue;

            String queryParams = "?fid_cond_mrkt_div_code=J" +
                    "&fid_input_iscd=" + stock.getCode() +
                    "&fid_input_date_1=" + startDate +
                    "&fid_input_date_2=" + endDate +
                    "&fid_period_div_code=D" +
                    "&fid_org_adj_prc=1";

            Map response = restClient.get()
                    .uri("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice" + queryParams)
                    .headers(setRequestHeaders("FHKST03010100"))
                    .retrieve()
                    .body(Map.class);

            Map<String, String> stockInfo = (Map<String, String>) response.get("output1");
            log.info("{}: {}", stockInfo.get("stck_shrn_iscd"), stockInfo.get("hts_kor_isnm"));
            List<Map<String, String>> daysData = (List<Map<String, String>>) response.get("output2");
            List<Chart> chartList = new ArrayList<>();
            for (Map<String, String> map : daysData) {
                if (map.isEmpty()) break;

                Chart chart = Chart.builder()
                        .date(convertDateStyle(map.get(DATE)))
                        .open(Integer.parseInt(map.get(OPEN)))
                        .close(Integer.parseInt(map.get(CLOSE)))
                        .high(Integer.parseInt(map.get(HIGH)))
                        .low(Integer.parseInt(map.get(LOW)))
                        .stock(stockRepository.findById(stockInfo.get("stck_shrn_iscd")).get())
                        .build();

                log.info(chart.toString());

                chartList.add(chart);
            }
//            batchRepository.batchInsert(chartList);
        }

    }

    private String convertDateStyle(String date) {
        DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate originDate = LocalDate.parse(date, originalFormatter);
        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        return originDate.format(newFormatter);
    }

    private Consumer<HttpHeaders> setRequestHeaders(String trId) {
        return httpHeaders -> {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setBearerAuth(authManager.generateToken());
            httpHeaders.set("appkey", authManager.getAppKey());
            httpHeaders.set("appsecret", authManager.getAppSecret());
            httpHeaders.set("tr_id", trId);
            httpHeaders.set("custtype", "P");
        };
    }
}
