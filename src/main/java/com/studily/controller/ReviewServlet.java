package com.studily.controller;

import com.studily.dao.ReviewDAO;
import com.studily.model.FlashcardReview;
import com.studily.model.User;
import com.studily.util.Flash;
import com.studily.util.Log;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Spaced-repetition study session. GET renders the due queue as a review
 * deck; POST records an Again/Hard/Good/Easy rating and reschedules.
 */
@WebServlet(name = "reviewServlet", urlPatterns = {"/review"})
public class ReviewServlet extends BaseAppServlet {

    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        try {
            List<FlashcardReview> due = reviewDAO.findDue(user.getUserId(), 30);
            request.setAttribute("dueCards", due);
            request.setAttribute("dueCount", reviewDAO.countDue(user.getUserId()));
            request.setAttribute("totalReviews", reviewDAO.countTotalReviews(user.getUserId()));
        } catch (SQLException e) {
            Log.severe("Review queue load failed for user " + user.getUserId() + ": " + e.getMessage(), e);
            Flash.error(request, "Could not load your review queue.");
            request.setAttribute("dueCards", List.of());
            request.setAttribute("dueCount", 0);
            request.setAttribute("totalReviews", 0);
        }
        request.setAttribute("navActive", "flashcards");
        render(request, response, "review.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        long cardId;
        int rating;
        try {
            cardId = Long.parseLong(request.getParameter("cardId"));
            rating = Integer.parseInt(request.getParameter("rating"));
        } catch (NumberFormatException e) {
            Flash.error(request, "Invalid review submission.");
            redirect(request, response, "/review");
            return;
        }
        if (rating < FlashcardReview.RATING_AGAIN || rating > FlashcardReview.RATING_EASY) {
            Flash.error(request, "Invalid rating.");
            redirect(request, response, "/review");
            return;
        }

        try {
            FlashcardReview r = reviewDAO.getOrCreate(user.getUserId(), (int) cardId);
            FlashcardReview.applyRating(r, rating);
            if (r.getId() == 0) {
                // State row was created inside getOrCreate but id not set on this path; persist insert.
                reviewDAO.update(r);
            } else {
                reviewDAO.update(r);
            }
        } catch (SQLException e) {
            Log.severe("Review save failed (card " + cardId + "): " + e.getMessage(), e);
            Flash.error(request, "Could not save your review.");
        }
        redirect(request, response, "/review");
    }
}
