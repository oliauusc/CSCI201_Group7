package com.foodlocator.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foodlocator.dao.LocationDAO;
import com.foodlocator.model.Location;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet for handling location API requests
 * Provides endpoints for retrieving location data from the database
 */
@WebServlet("/api/locations/*")
public class LocationServlet extends HttpServlet {
    
    private LocationDAO locationDAO;
    private Gson gson;
    
    @Override
    public void init() {
        locationDAO = new LocationDAO();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/locations - Get all locations
                getAllLocations(request, response);
            } else if (pathInfo.equals("/top")) {
                // GET /api/locations/top - Get top rated locations
                getTopLocations(request, response);
            } else {
                // GET /api/locations/{id} - Get specific location
                String[] parts = pathInfo.split("/");
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    try {
                        int locationId = Integer.parseInt(parts[1]);
                        getLocationById(locationId, request, response);
                    } catch (NumberFormatException e) {
                        sendError(response, "Invalid location ID", 400);
                    }
                } else {
                    sendError(response, "Invalid request", 400);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in LocationServlet: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Internal server error", 500);
        }
    }
    
    /**
     * Get all locations
     */
    private void getAllLocations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Location> locations = locationDAO.getAllLocations();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", locations);
            result.put("count", locations.size());
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            System.err.println("Error getting all locations: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Failed to retrieve locations", 500);
        }
    }
    
    /**
     * Get top rated locations
     */
    private void getTopLocations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Location> locations = locationDAO.getAllLocations();
            
            // Sort by rating and limit to top 3
            List<Location> topLocations = locations.stream()
                .filter(l -> l.getRating() > 0)
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                .limit(3)
                .toList();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", topLocations);
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            System.err.println("Error getting top locations: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Failed to retrieve top locations", 500);
        }
    }
    
    /**
     * Get specific location by ID
     */
    private void getLocationById(int locationId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Location location = locationDAO.getLocationById(locationId);
            
            Map<String, Object> result = new HashMap<>();
            
            if (location != null) {
                double rating = locationDAO.getAverageRating(locationId);
                location.setRating(rating);
                
                result.put("success", true);
                result.put("data", location);
            } else {
                result.put("success", false);
                result.put("error", "Location not found");
                response.setStatus(404);
            }
            
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            System.err.println("Error getting location by ID: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Failed to retrieve location", 500);
        }
    }
    
    /**
     * Send error response
     */
    private void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        
        response.getWriter().write(gson.toJson(error));
    }
}
