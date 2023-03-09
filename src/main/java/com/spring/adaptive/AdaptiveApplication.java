package com.spring.adaptive;

import com.spring.adaptive.properties.RateLimiterMetricProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(RateLimiterMetricProperties.class)
public class AdaptiveApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdaptiveApplication.class, args);
	}

}
