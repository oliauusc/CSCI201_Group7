<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>USCFOOD – Sign Up</title>
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
                <h1>Create your account ✨</h1>
                <p class="auth-subtitle">
                    Sign up to save restaurants, write reviews, and discover new spots.
                </p>
            </div>

            <% if (request.getAttribute("signupError") != null) { %>
                <div class="error-message">
                    <%= request.getAttribute("signupError") %>
                </div>
            <% } %>

            <form class="auth-form" action="${pageContext.request.contextPath}/signup" method="post">
                <label for="signup-email">Email</label>
                <input
                    type="email"
                    id="signup-email"
                    name="email"
                    class="input"
                    placeholder="uscid@usc.edu"
                    autocomplete="email"
                    required
                >

                <label for="signup-username">Username</label>
                <input
                    type="text"
                    id="signup-username"
                    name="username"
                    class="input"
                    placeholder="Choose a username"
                    autocomplete="username"
                    required
                >

                <label for="signup-password">Password</label>
                <input
                    type="password"
                    id="signup-password"
                    name="password"
                    class="input"
                    placeholder="Create a password"
                    autocomplete="new-password"
                    required
                >

                <label for="signup-confirm">Confirm password</label>
                <input
                    type="password"
                    id="signup-confirm"
                    name="confirm"
                    class="input"
                    placeholder="Re-enter your password"
                    autocomplete="new-password"
                    required
                >

                <button type="submit" class="btn btn-primary">
                    Sign up
                </button>

                <p class="auth-footer-text">
                    Already have an account?
                    <a href="${pageContext.request.contextPath}/jsp/login.jsp" class="auth-link">Log in</a>
                </p>
            </form>
        </section>
    </main>
</body>
</html>
