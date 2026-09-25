<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.YearMonth" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<%
    YearMonth month = (YearMonth) request.getAttribute("month");
    @SuppressWarnings("unchecked")
    List<Map.Entry<LocalDate, Integer>> days = (List<Map.Entry<LocalDate, Integer>>) request.getAttribute("days");
    LocalDate selectedDate = (LocalDate) request.getAttribute("selectedDate");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> selectedActivities = (List<Map<String, Object>>) request.getAttribute("selectedActivities");
    YearMonth prev = (YearMonth) request.getAttribute("prev");
    YearMonth next = (YearMonth) request.getAttribute("next");
    DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
    LocalDate today = LocalDate.now();
    // Pad the grid so day 1 lands on the right weekday column (Mon-first).
    int leadDays = month.atDay(1).getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
%>
<div class="container">
    <div class="page-head rise">
        <div class="flex-between">
            <div>
                <h1>Study Calendar</h1>
                <p>Your revision rhythm — click any day to see what you did.</p>
            </div>
            <div class="cal-nav">
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/calendar?y=<%= prev.getYear() %>&m=<%= prev.getMonthValue() %>"><svg class="i i-sm"><use href="#i-chevron-left"/></svg> <%= prev.format(DateTimeFormatter.ofPattern("MMM")) %></a>
                <strong><%= month.format(monthFmt) %></strong>
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/calendar?y=<%= next.getYear() %>&m=<%= next.getMonthValue() %>"><%= next.format(DateTimeFormatter.ofPattern("MMM")) %> <svg class="i i-sm"><use href="#i-chevron-right"/></svg></a>
            </div>
        </div>
    </div>

    <div class="cal-layout">
    <div class="card rise rise-1">
        <div class="cal-grid">
            <% for (String d : new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}) { %>
            <div class="cal-head"><%= d %></div>
            <% } %>
            <% for (int i = 0; i < leadDays; i++) { %><div class="cal-cell empty"></div><% } %>
            <% for (Map.Entry<LocalDate, Integer> entry : days) {
                   LocalDate date = entry.getKey();
                   boolean isToday = date.equals(today);
                   boolean isSelected = date.equals(selectedDate);
            %>
            <a class="cal-cell lvl-<%= entry.getValue() %> <%= isToday ? "today" : "" %> <%= isSelected ? "selected" : "" %>"
               href="<%= ctx %>/calendar?y=<%= month.getYear() %>&m=<%= month.getMonthValue() %>&day=<%= date %>">
                <span class="cal-day"><%= date.getDayOfMonth() %></span>
                <% if (isToday) { %><span class="cal-dot">•</span><% } %>
            </a>
            <% } %>
        </div>
        <div class="cal-legend text-dim">
            Less
            <span class="cal-cell lvl-0 static"></span>
            <span class="cal-cell lvl-1 static"></span>
            <span class="cal-cell lvl-2 static"></span>
            <span class="cal-cell lvl-3 static"></span>
            More
        </div>
    </div>

    <div class="card rise rise-2">
        <div class="card-title">
            <span class="icon"><svg class="i"><use href="#i-calendar"/></svg></span>
            <h3><%= selectedDate == null ? "Pick a day above" : "Activity on " + selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")) %></h3>
        </div>
        <% if (selectedActivities.isEmpty()) { %>
        <p class="text-dim"><%= selectedDate == null
                ? "Days you studied light up — click one to replay what happened."
                : (selectedDate.isAfter(today) ? "That day hasn't happened yet — plan a study session!" : "No activity recorded on this day.") %></p>
        <% } else { %>
            <% for (Map<String, Object> a : selectedActivities) { %>
        <div class="list-row">
            <div class="list-main">
                <div class="list-title"><svg class="i i-sm" style="color:var(--text-faint); margin-right:8px"><use href="#i-<%= a.get("icon") %>"/></svg><%= a.get("detail") %></div>
            </div>
            <div class="list-actions"><span class="badge"><%= a.get("extra") %></span></div>
        </div>
            <% } %>
        <% } %>
    </div>
    </div><%-- /.cal-layout --%>
</div>
<%@ include file="partials/footer.jsp" %>
