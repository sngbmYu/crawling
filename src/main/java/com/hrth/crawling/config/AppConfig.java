package com.hrth.crawling.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.function.Consumer;

@Configuration
public class AppConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .defaultHeaders(httpHeaders -> httpHeaders.set("Accept-Encoding", "gzip, deflate"));
    }

    @Bean
    public WebDriver webDriver() {
        WebDriverManager.chromedriver().browserVersion("127.0.6533.119").setup();
        return new ChromeDriver();
    }
}
