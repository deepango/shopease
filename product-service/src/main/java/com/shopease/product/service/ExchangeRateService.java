package com.shopease.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final String EXCHANGE_RATE_URL = "https://open.er-api.com/v6/latest/USD";

    private final RestTemplate restTemplate;

    public ExchangeRateService() {
        this.restTemplate = new RestTemplate();
    }

    @Cacheable(value = "exchangeRates", key = "'USD'")
    @SuppressWarnings("unchecked")
    public Map<String, Double> getUsdRates() {
        try {
            Map<String, Object> response = restTemplate.getForObject(EXCHANGE_RATE_URL, Map.class);
            if (response != null && response.containsKey("rates")) {
                return (Map<String, Double>) response.get("rates");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch exchange rates: {}", e.getMessage());
        }
        return Map.of("USD", 1.0, "INR", 83.0, "EUR", 0.92);
    }
}
