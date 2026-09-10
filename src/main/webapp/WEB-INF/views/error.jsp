<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="partials/head.jsp" %>
<%@ include file="partials/flash.jsp" %>
<div class="container-narrow" style="text-align:center; padding-top:80px">
    <div class="card rise" style="padding:56px 32px">
        <div style="font-size:3rem; margin-bottom:14px">⚠️</div>
        <h1>Something went wrong</h1>
        <p class="mt-1">We hit an unexpected error while loading this page. Your data is safe.</p>
        <div class="mt-3" style="display:flex; gap:12px; justify-content:center; flex-wrap:wrap">
            <a class="btn btn-primary" href="<%= ctx %>/dashboard">Back to Dashboard</a>
            <a class="btn btn-secondary" href="<%= ctx %>/upload">Upload Notes</a>
        </div>
    </div>
</div>
<%@ include file="partials/footer.jsp" %>
