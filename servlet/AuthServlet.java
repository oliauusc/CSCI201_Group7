package com.foodlocator.servlet;

import com.foodlocator.dao.UserDAO;
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
import java.util.Map;

/**
 * Servlet for handling authentication operations
 * Supports login, logout, and registration
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    
    private UserDAO userDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        gson = new Gson();
    }
    
    /**
     * Route requests based on path
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            sendError(response, "Invalid endpoint", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        switch (pathInfo) {
            case "/login":
                handleLogin(request, response);
                break;
            case "/register":
                handleRegister(request, response);
                break;
            case "/logout":
                handleLogout(request, response);
                break;
            default:
                sendError(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/current")) {
            handleGetCurrentUser(request, response);
        } else {
            sendError(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * POST /api/auth/login
     * Authenticates user and creates session
     * 
     * Request body (JSON):
     * {
     *   "email": "user@usc.edu",
     *   "password": "password123"
     * }
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
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
            
            String email = jsonRequest.get("email").getAsString();
            String password = jsonRequest.get("password").getAsString();
            
            // Validate input
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Email and password are required");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Validate USC email
            if (!email.endsWith("@usc.edu")) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Only USC email addresses are allowed");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Validate credentials
            User user = userDAO.validateLogin(email, password);
            
            if (user != null) {
                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(3600); // 1 hour
                
                // Create safe user object (no password)
                Map<String, Object> safeUser = new HashMap<>();
                safeUser.put("userID", user.getUserID());
                safeUser.put("email", user.getEmail());
                safeUser.put("name", user.getName());
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Login successful");
                responseData.put("user", safeUser);
                
                out.print(gson.toJson(responseData));
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Invalid email or password");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to process login");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
    
    /**
     * POST /api/auth/register
     * Creates a new user account
     * 
     * Request body (JSON):
     * {
     *   "email": "user@usc.edu",
     *   "name": "John Doe",
     *   "password": "password123"
     * }
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
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
            
            String email = jsonRequest.get("email").getAsString();
            String name = jsonRequest.get("name").getAsString();
            String password = jsonRequest.get("password").getAsString();
            
            // Validate input
            if (email == null || email.isEmpty() || 
                name == null || name.isEmpty() || 
                password == null || password.isEmpty()) {
                
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "All fields are required");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Validate USC email
            if (!email.endsWith("@usc.edu")) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Only USC email addresses are allowed");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Check if email already exists
            if (userDAO.emailExists(email)) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Email already registered");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
            
            // Validate password strength (basic)
            if (password.length() < 6) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Password must be at least 6 characters");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Create user
            // NOTE: In production, hash the password before storing!
            // Example: String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User newUser = new User(email, name, password);
            boolean created = userDAO.createUser(newUser);
            
            if (created) {
                // Create session automatically
                HttpSession session = request.getSession(true);
                session.setAttribute("user", newUser);
                session.setMaxInactiveInterval(3600); // 1 hour
                
                // Create safe user object (no password)
                Map<String, Object> safeUser = new HashMap<>();
                safeUser.put("userID", newUser.getUserID());
                safeUser.put("email", newUser.getEmail());
                safeUser.put("name", newUser.getName());
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Registration successful");
                responseData.put("user", safeUser);
                
                out.print(gson.toJson(responseData));
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "Failed to create account");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to process registration");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
    
    /**
     * POST /api/auth/logout
     * Destroys user session
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("success", true);
            successResponse.addProperty("message", "Logout successful");
            
            out.print(gson.toJson(successResponse));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to logout");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
    
    /**
     * GET /api/auth/current
     * Returns current logged-in user
     */
    private void handleGetCurrentUser(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session == null || session.getAttribute("user") == null) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("error", "No user logged in");
                
                out.print(gson.toJson(errorResponse));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            User user = (User) session.getAttribute("user");
            
            // Create safe user object (no password)
            Map<String, Object> safeUser = new HashMap<>();
            safeUser.put("userID", user.getUserID());
            safeUser.put("email", user.getEmail());
            safeUser.put("name", user.getName());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("user", safeUser);
            
            out.print(gson.toJson(responseData));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("error", "Failed to get current user");
            errorResponse.addProperty("message", e.getMessage());
            
            out.print(gson.toJson(errorResponse));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
    
    /**
     * Helper method to send error responses
     */
    private void sendError(HttpServletResponse response, String message, int status) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("success", false);
        errorResponse.addProperty("error", message);
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(errorResponse));
        response.setStatus(status);
        out.flush();
    }
}
