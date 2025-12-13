package com.foodlocator.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.foodlocator.dao.ReviewDAO;
import com.foodlocator.dao.UserDAO;
import com.foodlocator.model.Review;
import com.foodlocator.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet for handling review-related requests
 * Supports GET (retrieve reviews), POST (create review), DELETE (remove)
 */
@WebServlet("/api/reviews/*")
public class ReviewServlet extends HttpServlet {
    
    private ReviewDAO reviewDAO;
    private UserDAO userDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        reviewDAO = new ReviewDAO();
        userDAO = new UserDAO();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, "Location ID required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Parse path: /123 or /123/top
            String[] pathParts = pathInfo.substring(1).split("/");
            int locationId = Integer.parseInt(pathParts[0]);
            
            if (pathParts.length > 1 && "top".equals(pathParts[1])) {
                // GET /api/reviews/{locationId}/top - Get top 3 reviews
                getTopReviews(locationId, response, out);
            } else {
                // GET /api/reviews/{locationId}?page=1&pageSize=10&sortBy=recent
                getAllReviews(locationId, request, response, out);
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(response, out, "Invalid location ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error in doGet: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, out, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void getTopReviews(int locationId, HttpServletResponse response, PrintWriter out) {
        try {
            List<Review> reviews = reviewDAO.getTopReviewsByLocation(locationId, 3);
            
            // Transform reviews to include author name
            List<Map<String, Object>> transformedReviews = reviews.stream().map(review -> {
                Map<String, Object> reviewMap = new HashMap<>();
                User user = userDAO.getUserById(review.getUserID());
                String authorName = user != null ? user.getName() : "Anonymous";
                
                reviewMap.put("id", review.getReviewID());
                reviewMap.put("locationID", review.getLocationID());
                reviewMap.put("userID", review.getUserID());
                reviewMap.put("rating", review.getRating());
                reviewMap.put("title", review.getTitle());
                reviewMap.put("body", review.getBody());
                reviewMap.put("author", authorName);
                reviewMap.put("createdAt", review.getCreatedAt().toString());
                reviewMap.put("tags", new String[]{});
                reviewMap.put("helpfulCount", 0);
                
                return reviewMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("success", true);
            jsonResponse.put("data", transformedReviews);
            
            out.print(gson.toJson(jsonResponse));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            System.err.println("Error getting top reviews: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, out, "Error retrieving reviews", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void getAllReviews(int locationId, HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        try {
            String pageStr = request.getParameter("page");
            String pageSizeStr = request.getParameter("pageSize");
            String sortBy = request.getParameter("sortBy");
            
            int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
            int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "recent";
            }
            
            List<Review> reviews = reviewDAO.getReviewsByLocation(locationId);
            
            // Sort reviews based on sortBy parameter
            if ("rating-high".equals(sortBy)) {
                reviews.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
            } else if ("rating-low".equals(sortBy)) {
                reviews.sort((a, b) -> Double.compare(a.getRating(), b.getRating()));
            } else if ("helpful".equals(sortBy)) {
                // For now, sort by rating as a proxy for helpful
                reviews.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
            } else {
                // Default: "recent" - sort by createdAt descending
                reviews.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            }
            
            int totalReviews = reviews.size();
            int totalPages = (int) Math.ceil((double) totalReviews / pageSize);
            
            // Paginate
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, reviews.size());
            List<Review> pageReviews = reviews.subList(startIndex, endIndex);
            
            // Transform reviews to include author name
            List<Map<String, Object>> transformedReviews = pageReviews.stream().map(review -> {
                Map<String, Object> reviewMap = new HashMap<>();
                User user = userDAO.getUserById(review.getUserID());
                String authorName = user != null ? user.getName() : "Anonymous";
                
                reviewMap.put("id", review.getReviewID());
                reviewMap.put("locationID", review.getLocationID());
                reviewMap.put("userID", review.getUserID());
                reviewMap.put("rating", review.getRating());
                reviewMap.put("title", review.getTitle());
                reviewMap.put("body", review.getBody());
                reviewMap.put("author", authorName);
                reviewMap.put("createdAt", review.getCreatedAt().toString());
                reviewMap.put("tags", new String[]{});
                reviewMap.put("helpfulCount", 0);
                
                return reviewMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("reviews", transformedReviews);
            data.put("currentPage", page);
            data.put("totalPages", totalPages);
            data.put("totalReviews", totalReviews);
            data.put("pageSize", pageSize);
            
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("success", true);
            jsonResponse.put("data", data);
            
            out.print(gson.toJson(jsonResponse));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            System.err.println("Error getting all reviews: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, out, "Error retrieving reviews", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Allow both logged-in and anonymous submissions - use default user (1) if not logged in
            int userID = 1; // Default to user 1 (Sarah K.)
            
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object userIdObj = session.getAttribute("userId");
                if (userIdObj instanceof Integer) {
                    userID = (Integer) userIdObj;
                }
            }
            
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
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Rating must be between 0 and 5");
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (title == null || title.isEmpty() || body == null || body.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Title and body are required");
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Create review
            Review newReview = new Review(locationID, userID, rating, title, body);
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
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Failed to create review");
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            System.err.println("Error creating review: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, PrintWriter out, String message, int status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        out.print(gson.toJson(errorResponse));
        response.setStatus(status);
    }
}
