package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController(HolidayClient holidayClient) {
        this.holidayClient = holidayClient;
    }

    @GetMapping("/api/holidays")
    public ResponseEntity<Map<String, String>> getHolidays(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        HolidayClient.HolidayResult result = holidayClient.getHolidays();
        Map<String, String> holidays = result.holidays();
        Map<String, String> responseBody;
        if (from == null && to == null) {
            responseBody = holidays;
        } else {
            Map<String, String> filteredHolidays = new LinkedHashMap<>();
            holidays.forEach((date, name) -> {
                LocalDate holidayDate = LocalDate.parse(date);
                if ((from == null || !holidayDate.isBefore(from))
                        && (to == null || !holidayDate.isAfter(to))) {
                    filteredHolidays.put(date, name);
                }
            });
            responseBody = filteredHolidays;
        }

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.unavailable()) {
            response.header("X-Holidays-Unavailable", "true");
        }
        return response.body(responseBody);
    }
}
