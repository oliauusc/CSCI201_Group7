package com.foodlocator.servlet;

import com.foodlocator.dao.LocationDAO;
import com.foodlocator.dao.ReviewDAO;
import com.foodlocator.model.Location;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet for handling location-related requests
 * Supports GET (retrieve locations) and POST (create location)
 */
@WebServlet("/api/locations")
public class LocationServlet extends HttpServlet {
    
    private LocationDAO locationDAO;
    private ReviewDAO reviewDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        locationDAO = new LocationDAO();
        reviewDAO = new ReviewDAO();
        gson = new Gson();
    }
    
    /**
     * GET /api/locations
     * Returns all locations with their average ratings
     * 
     * Query parameters:
     * - category (optional): filter by category
     * - search (optional): search by name
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String category = request.getParameter("category");
            String search = request.getParameter("search");
            
            List<Location> locations;
            
            // Filter locations based on parameters
            if (category != null && !category.isEmpty()) {
                locations = locationDAO.getLocationsByCategory(category);
            } else if (search != null && !search.isEmpty()) {
                locations = locationDAO.searchLocationsByName(search);
            } else {
                locations = locationDAO.getAllLocations();
            }
            
            // Create response with locations and their ratings
            List<Map<String, Object>> locationData = new ArrayList<>();
            
            for (Location loc : locations) {
                Map<String, Object> locMap = new HashMap<>();
                locMap.put("locationID", loc.getLocationID());
                locMap.put("name", loc.getName());
                locMap.put("address", loc.getAddress());
                locMap.put("category", loc.getCategory());
                locMap.put("averageRating", reviewDAO.getAverageRating(loc.getLocationID()));
                locMap.put("reviewCount", reviewDAO.getReviewCount(loc.getLocationID()));
                locationData.add(locMap);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("count", locationData.size());
            responseData.put("locations", locationData);
            
            out.print(gson.toJson(responseData));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to retrieve locations");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
    
    /**
     * POST /api/locations
     * Creates a new location
     * 
     * Request body (JSON):
     * {
     *   "name": "Location Name",
     *   "address": "Address",
     *   "category": "Category"
     * }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Read request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            // Parse JSON
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            String name = jsonRequest.get("name").getAsString();
            String address = jsonRequest.get("address").getAsString();
            String category = jsonRequest.get("category").getAsString();
            
            // Validate input
            if (name == null || name.isEmpty() || 
                address == null || address.isEmpty() || 
                category == null || category.isEmpty()) {
                
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Missing required fields");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Create location
            Location newLocation = new Location(name, address, category);
            boolean created = locationDAO.createLocation(newLocation);
            
            if (created) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Location created successfully");
                responseData.put("locationID", newLocation.getLocationID());
                responseData.put("location", newLocation);
                
                out.print(gson.toJson(responseData));
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Failed to create location");
                
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
