package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month, Model model) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth selectedMonth = YearMonth.of(
                year == null ? currentMonth.getYear() : year,
                month == null ? currentMonth.getMonthValue() : month);
        YearMonth previousMonth = selectedMonth.minusMonths(1);
        YearMonth nextMonth = selectedMonth.plusMonths(1);

        List<List<LocalDate>> weeks = new ArrayList<>();
        List<LocalDate> week = new ArrayList<>();
        int leadingEmptyDays = selectedMonth.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < leadingEmptyDays; i++) {
            week.add(null);
        }

        for (int day = 1; day <= selectedMonth.lengthOfMonth(); day++) {
            week.add(selectedMonth.atDay(day));
            if (week.size() == 7) {
                weeks.add(week);
                week = new ArrayList<>();
            }
        }
        while (!week.isEmpty() && week.size() < 7) {
            week.add(null);
        }
        if (!week.isEmpty()) {
            weeks.add(week);
        }

        model.addAttribute("year", selectedMonth.getYear());
        model.addAttribute("month", selectedMonth.getMonthValue());
        model.addAttribute("firstDay", selectedMonth.atDay(1));
        model.addAttribute("lastDay", selectedMonth.atEndOfMonth());
        model.addAttribute("weeks", weeks);
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        return "calendar";
    }
}
