<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%
    List<String[]> flashList = (List<String[]>) request.getAttribute("flashMessages");
    if (flashList != null && !flashList.isEmpty()) {
%>
<div class="flash-stack" id="flash-stack">
    <% for (String[] f : flashList) { %>
    <div class="flash <%= f[0] %>" data-type="<%= f[0] %>">
        <span><%= f[1] %></span>
    </div>
    <% } %>
</div>
<% } %>
