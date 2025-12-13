package com.foodlocator.dao;

import com.foodlocator.model.Review;
import com.foodlocator.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Review operations
 * Handles all database operations related to reviews
 */
public class ReviewDAO {
    
    private DatabaseConnection dbConnection;
    
    public ReviewDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create a new review in the database
     * @param review Review object to insert
     * @return true if successful, false otherwise
     */
    public boolean createReview(Review review) {
        String sql = "INSERT INTO Reviews (locationID, userID, rating, title, body) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, review.getLocationID());
            pstmt.setInt(2, review.getUserID());
            pstmt.setDouble(3, review.getRating());
            pstmt.setString(4, review.getTitle());
            pstmt.setString(5, review.getBody());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated reviewID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        review.setReviewID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating review: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get review by reviewID
     * @param reviewID ID of the review
     * @return Review object or null if not found
     */
    public Review getReviewById(int reviewID) {
        String sql = "SELECT * FROM Reviews WHERE reviewID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reviewID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Review(
                        rs.getInt("reviewID"),
                        rs.getInt("locationID"),
                        rs.getInt("userID"),
                        rs.getDouble("rating"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getTimestamp("createdAt")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting review by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all reviews for a specific location
     * @param locationID ID of the location
     * @return List of reviews for that location
     */
    public List<Review> getReviewsByLocation(int locationID) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE locationID = ? ORDER BY createdAt DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("reviewID"),
                        rs.getInt("locationID"),
                        rs.getInt("userID"),
                        rs.getDouble("rating"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting reviews by location: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }
    
    /**
     * Get all reviews by a specific user
     * @param userID ID of the user
     * @return List of reviews by that user
     */
    public List<Review> getReviewsByUser(int userID) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE userID = ? ORDER BY createdAt DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("reviewID"),
                        rs.getInt("locationID"),
                        rs.getInt("userID"),
                        rs.getDouble("rating"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting reviews by user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }
    
    /**
     * Get top N reviews for a location (sorted by rating)
     * @param locationID ID of the location
     * @param limit Number of reviews to return
     * @return List of top reviews
     */
    public List<Review> getTopReviewsByLocation(int locationID, int limit) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE locationID = ? ORDER BY rating DESC, createdAt DESC LIMIT ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("reviewID"),
                        rs.getInt("locationID"),
                        rs.getInt("userID"),
                        rs.getDouble("rating"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getTimestamp("createdAt")
                    ));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting top reviews: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }
    
    /**
     * Get average rating for a location
     * @param locationID ID of the location
     * @return Average rating or 0 if no reviews
     */
    public double getAverageRating(int locationID) {
        String sql = "SELECT AVG(rating) as avgRating FROM Reviews WHERE locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avgRating");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting average rating: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Get review count for a location
     * @param locationID ID of the location
     * @return Number of reviews
     */
    public int getReviewCount(int locationID) {
        String sql = "SELECT COUNT(*) as reviewCount FROM Reviews WHERE locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("reviewCount");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting review count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Update review
     * @param review Review object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateReview(Review review) {
        String sql = "UPDATE Reviews SET rating = ?, title = ?, body = ? WHERE reviewID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, review.getRating());
            pstmt.setString(2, review.getTitle());
            pstmt.setString(3, review.getBody());
            pstmt.setInt(4, review.getReviewID());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete review by ID
     * @param reviewID ID of the review to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteReview(int reviewID) {
        String sql = "DELETE FROM Reviews WHERE reviewID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reviewID);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Check if user has already reviewed a location
     * @param userID ID of the user
     * @param locationID ID of the location
     * @return true if user has reviewed, false otherwise
     */
    public boolean hasUserReviewed(int userID, int locationID) {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE userID = ? AND locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking if user has reviewed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get all reviews (for admin purposes)
     * @return List of all reviews
     */
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews ORDER BY createdAt DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                reviews.add(new Review(
                    rs.getInt("reviewID"),
                    rs.getInt("locationID"),
                    rs.getInt("userID"),
                    rs.getDouble("rating"),
                    rs.getString("title"),
                    rs.getString("body"),
                    rs.getTimestamp("createdAt")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all reviews: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }
}
