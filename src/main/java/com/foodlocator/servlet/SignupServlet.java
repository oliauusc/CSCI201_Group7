package com.foodlocator.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

import com.foodlocator.util.DatabaseConnection;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Read parameters FIRST before any output
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");

        // ==== BASIC FIELD VALIDATION ====
        if (email == null || username == null || password == null || confirm == null ||
            email.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            request.setAttribute("signupError", "All fields must be filled.");
            request.getRequestDispatcher("loginSignup.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirm)) {
            request.setAttribute("signupError", "Passwords do not match.");
            request.getRequestDispatcher("loginSignup.jsp").forward(request, response);
            return;
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // ==== CHECK EMAIL ====
            PreparedStatement checkEmail = conn.prepareStatement(
                "SELECT id FROM users WHERE email = ?"
            );
            checkEmail.setString(1, email);
            ResultSet rs1 = checkEmail.executeQuery();

            if (rs1.next()) {
                request.setAttribute("signupError", "Email already exists.");
                request.getRequestDispatcher("loginSignup.jsp").forward(request, response);
                return;
            }

            // ==== CHECK USERNAME ====
            PreparedStatement checkUser = conn.prepareStatement(
                "SELECT id FROM users WHERE username = ?"
            );
            checkUser.setString(1, username);
            ResultSet rs2 = checkUser.executeQuery();

            if (rs2.next()) {
                request.setAttribute("signupError", "Username already exists.");
                request.getRequestDispatcher("loginSignup.jsp").forward(request, response);
                return;
            }

            // ==== CREATE USER ====
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO users (email, username, password) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, email);
            insert.setString(2, username);
            insert.setString(3, password);
            insert.executeUpdate();

            ResultSet keys = insert.getGeneratedKeys();
            keys.next();
            int userId = keys.getInt(1);

            // Create session and redirect
            HttpSession session = request.getSession();
            session.setAttribute("userId", userId);
            session.setAttribute("username", username);

            response.sendRedirect("index.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("SIGNUP ERROR: " + e.getMessage());
            e.printStackTrace(System.err);
            request.setAttribute("signupError", "Signup failed: " + e.getMessage());
            try {
                request.getRequestDispatcher("loginSignup.jsp").forward(request, response);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
