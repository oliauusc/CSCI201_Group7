package com.foodlocator.servlet;

import com.foodlocator.dao.ReviewDAO;
import com.foodlocator.model.Review;
import com.foodlocator.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet for handling review-related requests
 * Supports GET (retrieve reviews), POST (create review), PUT (update), DELETE (remove)
 */
@WebServlet("/api/reviews")
public class ReviewServlet extends HttpServlet {
    
    private ReviewDAO reviewDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        reviewDAO = new ReviewDAO();
        gson = new Gson();
    }
    
    /**
     * GET /api/reviews
     * Returns reviews for a specific location or user
     * 
     * Query parameters:
     * - locationID: get reviews for a location
     * - userID: get reviews by a user
     * - limit: limit number of top reviews (used with locationID)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String locationIDStr = request.getParameter("locationID");
            String userIDStr = request.getParameter("userID");
            String limitStr = request.getParameter("limit");
            
            List<Review> reviews;
            
            if (locationIDStr != null) {
                int locationID = Integer.parseInt(locationIDStr);
                
                if (limitStr != null) {
                    int limit = Integer.parseInt(limitStr);
                    reviews = reviewDAO.getTopReviewsByLocation(locationID, limit);
                } else {
                    reviews = reviewDAO.getReviewsByLocation(locationID);
                }
                
            } else if (userIDStr != null) {
                int userID = Integer.parseInt(userIDStr);
                reviews = reviewDAO.getReviewsByUser(userID);
                
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Missing required parameter: locationID or userID");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("count", reviews.size());
            responseData.put("reviews", reviews);
            
            out.print(gson.toJson(responseData));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (NumberFormatException e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Invalid parameter format");
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to retrieve reviews");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
    
    /**
     * POST /api/reviews
     * Creates a new review
     * 
     * Request body (JSON):
     * {
     *   "locationID": 1,
     *   "rating": 4.5,
     *   "title": "Great place!",
     *   "body": "I loved the food here..."
     * }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Check if user is logged in
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "User not authenticated");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            User user = (User) session.getAttribute("user");
            
            // Read request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            // Parse JSON
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            int locationID = jsonRequest.get("locationID").getAsInt();
            double rating = jsonRequest.get("rating").getAsDouble();
            String title = jsonRequest.get("title").getAsString();
            String body = jsonRequest.get("body").getAsString();
            
            // Validate input
            if (rating < 0 || rating > 5) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Rating must be between 0 and 5");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (title == null || title.isEmpty() || body == null || body.isEmpty()) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Title and body are required");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Check if user has already reviewed this location
            if (reviewDAO.hasUserReviewed(user.getUserID(), locationID)) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "You have already reviewed this location");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
            
            // Create review
            Review newReview = new Review(locationID, user.getUserID(), rating, title, body);
            boolean created = reviewDAO.createReview(newReview);
            
            if (created) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Review created successfully");
                responseData.put("reviewID", newReview.getReviewID());
                responseData.put("review", newReview);
                
                out.print(gson.toJson(responseData));
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Failed to create review");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to process request");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
    
    /**
     * DELETE /api/reviews
     * Deletes a review
     * 
     * Query parameter:
     * - reviewID: ID of review to delete
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Check if user is logged in
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "User not authenticated");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            User user = (User) session.getAttribute("user");
            
            String reviewIDStr = request.getParameter("reviewID");
            if (reviewIDStr == null) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Missing required parameter: reviewID");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            int reviewID = Integer.parseInt(reviewIDStr);
            
            // Verify user owns this review
            Review review = reviewDAO.getReviewById(reviewID);
            if (review == null) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Review not found");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if (review.getUserID() != user.getUserID()) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "You can only delete your own reviews");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // Delete review
            boolean deleted = reviewDAO.deleteReview(reviewID);
            
            if (deleted) {
                JsonObject successResponse = new JsonObject();
                successResponse.addProperty("success", true);
                successResponse.addProperty("message", "Review deleted successfully");
                
                out.print(gson.toJson(successResponse));
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Failed to delete review");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to process request");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
}
