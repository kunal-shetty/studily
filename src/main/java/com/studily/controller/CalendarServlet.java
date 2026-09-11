package com.studily.controller;

import com.studily.dao.EngagementDAO;
import com.studily.model.User;
import com.studily.util.Flash;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Smart revision calendar: month grid with per-day activity level plus a
 * drill-down list for a selected day.
 */
@WebServlet(name = "calendarServlet", urlPatterns = {"/calendar"})
public class CalendarServlet extends BaseAppServlet {

    private final EngagementDAO engagementDAO = new EngagementDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);

        YearMonth month = YearMonth.now();
        Integer y = com.studily.util.Validators.parseIntOrNull(request.getParameter("y"));
        Integer m = com.studily.util.Validators.parseIntOrNull(request.getParameter("m"));
        if (y != null && m != null && m >= 1 && m <= 12 && y >= 2020 && y <= 2100) {
            month = YearMonth.of(y, m);
        }

        // Day -> activity level 0-3, one SQL round-trip for the whole month.
        Map<LocalDate, Integer> levels;
        try {
            levels = engagementDAO.monthActivity(user.getUserId(), month.getYear(), month.getMonthValue());
        } catch (Exception e) {
            Log.severe("Calendar month load failed: " + e.getMessage(), e);
            levels = Map.of();
        }
        List<Map.Entry<LocalDate, Integer>> days = new ArrayList<>();
        for (int d = 1; d <= month.lengthOfMonth(); d++) {
            LocalDate date = month.atDay(d);
            days.add(new AbstractMap.SimpleEntry<>(date, levels.getOrDefault(date, 0)));
        }

        String selected = request.getParameter("day");
        LocalDate selectedDate = null;
        List<Map<String, Object>> selectedActivities = List.of();
        if (selected != null) {
            try {
                selectedDate = LocalDate.parse(selected);
                if (!selectedDate.isAfter(LocalDate.now().plusDays(1))) {
                    selectedActivities = engagementDAO.dayActivity(user.getUserId(), selectedDate);
                }
            } catch (Exception e) {
                Flash.error(request, "Invalid date.");
            }
        }

        request.setAttribute("month", month);
        request.setAttribute("days", days);
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("selectedActivities", selectedActivities);
        request.setAttribute("prev", month.minusMonths(1));
        request.setAttribute("next", month.plusMonths(1));
        request.setAttribute("navActive", "calendar");
        render(request, response, "calendar.jsp");
    }

}
