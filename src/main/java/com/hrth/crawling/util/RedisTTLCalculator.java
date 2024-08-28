package com.hrth.crawling.util;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RedisTTLCalculator {
    public static long calculateTTLForMidnightKST(ZonedDateTime zonedDateTime) {
        // 한국 표준시 기준
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Duration duration = Duration.between(now, zonedDateTime);
        return duration.getSeconds();
    }
}
