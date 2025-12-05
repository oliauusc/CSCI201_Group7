package com.foodlocator.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/checkSession")
public class CheckSessionServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("username") != null) {
            // User is logged in
            PrintWriter out = response.getWriter();
            String username = (String) session.getAttribute("username");
            Integer userId = (Integer) session.getAttribute("userId");
            
            out.print("{\"loggedIn\": true, \"username\": \"" + username + "\", \"userId\": " + userId + "}");
        } else {
            // User is not logged in
            PrintWriter out = response.getWriter();
            out.print("{\"loggedIn\": false}");
        }
    }
}
