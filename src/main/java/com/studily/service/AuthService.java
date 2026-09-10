package com.studily.service;

import com.studily.dao.UserDAO;
import com.studily.model.User;
import com.studily.util.Log;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * Authentication business logic: BCrypt hashing on register, BCrypt
 * verification on login. Servlets stay thin.
 */
public final class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public AuthService() {
    }

    /** @throws AuthException with a user-friendly message on any failure */
    public User register(String name, String email, String password, String confirmPassword)
            throws AuthException {
        if (!com.studily.util.Validators.isValidName(name)) {
            throw new AuthException("Please enter your full name (2-100 characters).");
        }
        if (!com.studily.util.Validators.isValidEmail(email)) {
            throw new AuthException("Please enter a valid email address.");
        }
        if (!com.studily.util.Validators.isStrongPassword(password)) {
            throw new AuthException("Password must be at least 8 characters with letters and numbers.");
        }
        if (!password.equals(confirmPassword)) {
            throw new AuthException("Passwords do not match.");
        }
        try {
            if (userDAO.emailExists(email.trim())) {
                throw new AuthException("An account with this email already exists.");
            }
            User user = new User();
            user.setName(name.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
            user.setRole("student");
            if (!userDAO.create(user)) {
                throw new AuthException("Could not create the account. Please try again.");
            }
            return userDAO.findByEmail(email.trim());
        } catch (SQLException e) {
            Log.severe("Registration failed for " + email + ": " + e.getMessage(), e);
            throw new AuthException("Database error during registration. Please try again.");
        }
    }

    /** @throws AuthException with a generic failure message on bad credentials */
    public User login(String email, String password) throws AuthException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AuthException("Please enter both email and password.");
        }
        try {
            User user = userDAO.findByEmail(email.trim());
            // Constant-ish response regardless of whether the email exists.
            if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
                throw new AuthException("Invalid email or password.");
            }
            return user;
        } catch (SQLException e) {
            Log.severe("Login failed for " + email + ": " + e.getMessage(), e);
            throw new AuthException("Database error during login. Please try again.");
        }
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
