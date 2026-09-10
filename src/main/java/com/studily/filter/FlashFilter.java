package com.studily.filter;

import com.studily.util.Flash;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Renders one-shot flash messages. FlashFilter drains the session list on
 * every request and exposes it as a request attribute for JSPs.
 */
public class FlashFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        List<String[]> messages = Flash.drain(request);
        request.setAttribute("flashMessages", messages);

        chain.doFilter(request, response);
    }
}
