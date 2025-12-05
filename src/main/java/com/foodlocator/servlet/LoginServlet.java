package com.foodlocator.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

import com.foodlocator.util.DatabaseConnection;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Read parameters FIRST before calling getWriter()
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, username FROM users WHERE username = ? AND password = ?"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Login successful - create session and redirect
                HttpSession session = request.getSession();
                session.setAttribute("userId", rs.getInt("id"));
                session.setAttribute("username", rs.getString("username"));
                response.sendRedirect("index.jsp");
            } else {
                // Login failed - forward with error message
                request.setAttribute("loginError", "Invalid username or password");
                request.getRequestDispatcher("loginSignup.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("loginError", "Login failed");
            request.getRequestDispatcher("loginSignup.jsp").forward(request, response);
        }
    }
}
