package com.studily.controller;

import com.studily.filter.AuthFilter;
import com.studily.model.User;
import com.studily.service.AuthService;
import com.studily.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Login: GET renders the form, POST authenticates.
 */
@WebServlet(name = "loginServlet", urlPatterns = {"/login"})
public class LoginServlet extends BaseAppServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute(AuthFilter.SESSION_USER) != null) {
            redirect(request, response, "/dashboard");
            return;
        }
        render(request, response, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = authService.login(email, password);
            // Rotate session id to prevent session fixation.
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER, user);
            Flash.success(request, "Welcome back, " + user.getName() + "!");
            redirect(request, response, "/dashboard");
        } catch (AuthService.AuthException e) {
            Flash.error(request, e.getMessage());
            request.getSession().setAttribute("prefillEmail", email == null ? "" : email);
            redirect(request, response, "/login");
        }
    }
}
