package com.foodlocator.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.foodlocator.model.Location;
import com.foodlocator.util.DatabaseConnection;

/**
 * Data Access Object for Location operations
 * Handles all database operations related to food locations
 */
public class LocationDAO {
    
    private DatabaseConnection dbConnection;
    
    public LocationDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create a new location in the database
     * @param location Location object to insert
     * @return true if successful, false otherwise
     */
    public boolean createLocation(Location location) {
        String sql = "INSERT INTO Locations (name, address, category) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, location.getName());
            pstmt.setString(2, location.getAddress());
            pstmt.setString(3, location.getCategory());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated locationID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        location.setLocationID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating location: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get location by locationID
     * @param locationID ID of the location
     * @return Location object or null if not found
     */
    public Location getLocationById(int locationID) {
        String sql = "SELECT * FROM Locations WHERE locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Location(
                        rs.getInt("locationID"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("category")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting location by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all locations
     * @return List of all locations
     */
    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM Locations ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Location loc = new Location(
                    rs.getInt("locationID"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("category"),
                    rs.getDouble("lat"),
                    rs.getDouble("lng")
                );
                
                // Get average rating from reviews
                double avgRating = getAverageRating(rs.getInt("locationID"));
                loc.setRating(avgRating);
                loc.setDescription(rs.getString("category"));
                
                // Get review count
                int reviewCount = getReviewCount(rs.getInt("locationID"));
                loc.setReviewCount(reviewCount);
                
                locations.add(loc);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all locations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return locations;
    }
    
    /**
     * Get locations by category
     * @param category Category to filter by
     * @return List of locations in that category
     */
    public List<Location> getLocationsByCategory(String category) {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM Locations WHERE category = ? ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    locations.add(new Location(
                        rs.getInt("locationID"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("category")
                    ));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting locations by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return locations;
    }
    
    /**
     * Search locations by name
     * @param searchTerm Term to search for in location names
     * @return List of matching locations
     */
    public List<Location> searchLocationsByName(String searchTerm) {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM Locations WHERE name LIKE ? ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    locations.add(new Location(
                        rs.getInt("locationID"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("category")
                    ));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching locations by name: " + e.getMessage());
            e.printStackTrace();
        }
        
        return locations;
    }
    
    /**
     * Update location information
     * @param location Location object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateLocation(Location location) {
        String sql = "UPDATE Locations SET name = ?, address = ?, category = ? WHERE locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, location.getName());
            pstmt.setString(2, location.getAddress());
            pstmt.setString(3, location.getCategory());
            pstmt.setInt(4, location.getLocationID());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating location: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete location by ID
     * @param locationID ID of the location to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteLocation(int locationID) {
        String sql = "DELETE FROM Locations WHERE locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting location: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get distinct categories
     * @return List of all unique categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM Locations ORDER BY category";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
    
    /**
     * Get location with average rating
     * @param locationID ID of the location
     * @return Object array [Location, avgRating] or null if not found
     */
    public Object[] getLocationWithRating(int locationID) {
        String sql = "SELECT l.*, COALESCE(AVG(r.rating), 0) as avgRating " +
                    "FROM Locations l " +
                    "LEFT JOIN Reviews r ON l.locationID = r.locationID " +
                    "WHERE l.locationID = ? " +
                    "GROUP BY l.locationID";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Location location = new Location(
                        rs.getInt("locationID"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("category")
                    );
                    double avgRating = rs.getDouble("avgRating");
                    return new Object[]{location, avgRating};
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting location with rating: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get average rating for a location
     * @param locationID ID of the location
     * @return Average rating or 0.0 if no reviews
     */
    public double getAverageRating(int locationID) {
        String sql = "SELECT AVG(rating) as avgRating FROM reviews WHERE locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double rating = rs.getDouble("avgRating");
                    return Double.isNaN(rating) || rating == 0 ? 0.0 : Math.round(rating * 10.0) / 10.0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting average rating: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    /**
     * Get review count for a location
     * @param locationID ID of the location
     * @return Number of reviews for the location
     */
    public int getReviewCount(int locationID) {
        String sql = "SELECT COUNT(*) as count FROM reviews WHERE locationID = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, locationID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting review count: " + e.getMessage());
        }
        
        return 0;
    }
}
