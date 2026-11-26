package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
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
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false, defaultValue = "month") String view,
            @RequestParam(required = false) LocalDate date, Model model) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth requestedMonth = YearMonth.of(
                year == null ? currentMonth.getYear() : year,
                month == null ? currentMonth.getMonthValue() : month);
        view = "week".equals(view) ? "week" : "month";
        LocalDate selectedDate = date == null ? requestedMonth.atDay(1) : date;
        YearMonth selectedMonth = YearMonth.from(selectedDate);
        YearMonth previousMonth = selectedMonth.minusMonths(1);
        YearMonth nextMonth = selectedMonth.plusMonths(1);

        List<List<LocalDate>> weeks = new ArrayList<>();
        LocalDate firstDay;
        LocalDate lastDay;
        LocalDate previousWeekStart = null;
        LocalDate nextWeekStart = null;

        if ("week".equals(view)) {
            firstDay = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            lastDay = firstDay.plusDays(6);
            previousWeekStart = firstDay.minusWeeks(1);
            nextWeekStart = firstDay.plusWeeks(1);
            List<LocalDate> week = new ArrayList<>(7);
            for (int day = 0; day < 7; day++) {
                week.add(firstDay.plusDays(day));
            }
            weeks.add(week);
        } else {
            firstDay = selectedMonth.atDay(1);
            lastDay = selectedMonth.atEndOfMonth();
            List<LocalDate> week = new ArrayList<>();
            int leadingEmptyDays = firstDay.getDayOfWeek().getValue() % 7;
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
        }

        model.addAttribute("year", selectedMonth.getYear());
        model.addAttribute("month", selectedMonth.getMonthValue());
        model.addAttribute("view", view);
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        model.addAttribute("previousWeekStart", previousWeekStart);
        model.addAttribute("nextWeekStart", nextWeekStart);
        model.addAttribute("weeks", weeks);
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        return "calendar";
    }
}
