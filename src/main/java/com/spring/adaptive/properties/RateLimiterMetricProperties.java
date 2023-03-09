package com.spring.adaptive.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterMetricProperties {


    private Map<String, RateLimiterMetricDetailProperties> metrics = new HashMap<>();

    public Map<String, RateLimiterMetricDetailProperties> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, RateLimiterMetricDetailProperties> metrics) {
        this.metrics = metrics;
    }

    public static class RateLimiterMetricDetailProperties {

        private int maxLimitForPeriod = 10;
        private int minLimitForPeriod = 10;
        private Duration limitRefreshPeriod = Duration.ofMinutes(1);
        private Duration slowDuration = Duration.ofMillis(500);

        public int getMaxLimitForPeriod() {
            return maxLimitForPeriod;
        }

        public void setMaxLimitForPeriod(int maxLimitForPeriod) {
            this.maxLimitForPeriod = maxLimitForPeriod;
        }

        public int getMinLimitForPeriod() {
            return minLimitForPeriod;
        }

        public void setMinLimitForPeriod(int minLimitForPeriod) {
            this.minLimitForPeriod = minLimitForPeriod;
        }

        public Duration getLimitRefreshPeriod() {
            return limitRefreshPeriod;
        }

        public void setLimitRefreshPeriod(Duration limitRefreshPeriod) {
            this.limitRefreshPeriod = limitRefreshPeriod;
        }

        public Duration getSlowDuration() {
            return slowDuration;
        }

        public void setSlowDuration(Duration slowDuration) {
            this.slowDuration = slowDuration;
        }
    }
}
