<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>USCFOOD – Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="usc-auth-body">
    <!-- NAVBAR -->
    <header class="usc-nav">
        <div class="usc-nav-left">
            <div class="usc-logo">USC<span>FOOD</span></div>
        </div>
        <nav class="usc-nav-right">
            <a href="${pageContext.request.contextPath}/" class="nav-pill nav-pill-outline">Map</a>
            <a href="list.html" class="nav-pill nav-pill-outline">List</a>
            <a href="${pageContext.request.contextPath}/jsp/login.jsp" class="nav-pill nav-pill-primary">Login</a>
        </nav>
    </header>

    <!-- MAIN AUTH CARD -->
    <main class="auth-shell">
        <section class="auth-card">
            <div class="auth-header">
                <h1>Welcome back 👋</h1>
                <p class="auth-subtitle">
                    Log in to save spots, leave reviews, and find food near USC faster.
                </p>
            </div>

            <% if (request.getAttribute("loginError") != null) { %>
                <div class="error-message">
                    <%= request.getAttribute("loginError") %>
                </div>
            <% } %>

            <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">
                <label for="login-username">Username</label>
                <input
                    type="text"
                    id="login-username"
                    name="username"
                    class="input"
                    placeholder="Enter your username"
                    autocomplete="username"
                    required
                >

                <label for="login-password">Password</label>
                <input
                    type="password"
                    id="login-password"
                    name="password"
                    class="input"
                    placeholder="Enter your password"
                    autocomplete="current-password"
                    required
                >

                <button type="submit" class="btn btn-primary">
                    Log in
                </button>

                <p class="auth-footer-text">
                    New here?
                    <a href="${pageContext.request.contextPath}/jsp/signup.jsp" class="auth-link">Create an account</a>
                </p>
            </form>
        </section>
    </main>
</body>
</html>
