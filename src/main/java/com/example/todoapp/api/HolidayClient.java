package com.example.todoapp.api;

import java.time.Duration;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HolidayClient {

    private final RestClient restClient = createRestClient();

    public HolidayResult getHolidays() {
        try {
            Map<String, String> holidays = restClient.get()
                    .uri("https://holidays-jp.github.io/api/v1/date.json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
            if (holidays == null) {
                return new HolidayResult(Map.of(), true);
            }
            return new HolidayResult(holidays, false);
        } catch (RestClientException exception) {
            return new HolidayResult(Map.of(), true);
        }
    }

    private static RestClient createRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}
