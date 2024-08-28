package com.hrth.crawling.service;

import com.hrth.crawling.entity.Stock;
import com.hrth.crawling.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.StringTokenizer;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataInitService {
    private final StockRepository stockRepository;

    public void init() throws Exception {
        File file = new File("/Users/yuseungbeom/Downloads/stocks_information.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "EUC-KR"));

        String line;
        StringTokenizer st;
        boolean isFirstLine = true;
        while ((line = br.readLine()) != null) {

            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }

            log.info(line);
            st = new StringTokenizer(line, ",");

            String code = st.nextToken().replace("\"", "");
            String name = st.nextToken().replace("\"", "");

            log.info("code: {}, name: {}", code, name);

            stockRepository.save(Stock.builder()
                    .code(code)
                    .name(name)
                    .build());
        }
    }
}
