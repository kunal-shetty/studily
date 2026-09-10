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
 * Registration: GET renders the form, POST creates the account.
 */
@WebServlet(name = "registerServlet", urlPatterns = {"/register"})
public class RegisterServlet extends BaseAppServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Already logged in? Go straight to the dashboard.
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute(AuthFilter.SESSION_USER) != null) {
            redirect(request, response, "/dashboard");
            return;
        }
        render(request, response, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            User user = authService.register(name, email, password, confirmPassword);
            HttpSession session = request.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER, user);
            Flash.success(request, "Welcome to Studily, " + user.getName() + "!");
            redirect(request, response, "/dashboard");
        } catch (AuthService.AuthException e) {
            Flash.error(request, e.getMessage());
            // Keep the entered email/name so the user can fix quickly.
            request.getSession().setAttribute("prefillName", name == null ? "" : name);
            request.getSession().setAttribute("prefillEmail", email == null ? "" : email);
            redirect(request, response, "/register");
        }
    }
}
