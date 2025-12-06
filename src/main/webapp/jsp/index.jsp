<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
    // Check if user is logged in
    String username = (String) session.getAttribute("username");
    Integer userId = (Integer) session.getAttribute("userId");
    boolean isLoggedIn = (username != null && userId != null);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>USC Food Finder</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <script>
        // Pass login status to JavaScript
        window.isLoggedIn = <%= isLoggedIn %>;
        window.currentUser = <%= isLoggedIn ? "{username: '" + username + "', userId: " + userId + "}" : "null" %>;
    </script>
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-content">
            <div class="logo">
                <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                    <circle cx="16" cy="16" r="14" fill="currentColor" opacity="0.1"/>
                    <path d="M16 8L20 14H12L16 8Z" fill="currentColor"/>
                    <circle cx="16" cy="18" r="2" fill="currentColor"/>
                </svg>
                <span class="logo-text">USC<span class="logo-accent">FOOD</span></span>
            </div>
            <nav class="nav">
                <button class="nav-btn active" data-view="map">Map</button>
                <button class="nav-btn" data-view="list">List</button>
                <% if (isLoggedIn) { %>
                    <a href="<%= request.getContextPath() %>/logout" class="login-btn" id="loginBtn" style="color: #ff6b6b;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                        </svg>
                        Logout
                    </a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jsp/login.jsp" class="login-btn" id="loginBtn">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                        </svg>
                        Login
                    </a>
                <% } %>
            </nav>
        </div>
    </header>

    <!-- Main Content -->
    <main class="main-container">
        <!-- Map View -->
        <div class="map-view" id="mapView">
            <div id="map" class="map-canvas"></div>
            
            <!-- Locate Me Button (floating on map) -->
            <button class="location-btn-floating" id="locateBtn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <circle cx="12" cy="12" r="10"/>
                    <circle cx="12" cy="12" r="3" fill="currentColor"/>
                </svg>
                <span>Locate Me</span>
            </button>
            
            <!-- Nearby Sidebar -->
            <aside class="nearby-sidebar">
                <div class="sidebar-header">
                    <h2 class="sidebar-title">Nearby</h2>
                    <div class="distance-badge" id="userDistanceIndicator">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <circle cx="12" cy="12" r="10"/>
                            <circle cx="12" cy="12" r="3" fill="currentColor"/>
                        </svg>
                        <span>USC Campus</span>
                    </div>
                </div>
                
                <div class="filter-row">
                    <select class="filter-select" id="sortSelect">
                        <option value="distance">Distance</option>
                        <option value="rating">Rating</option>
                        <option value="popular">Popular</option>
                    </select>
                    <button class="filter-toggle" id="filterBtn">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <line x1="4" y1="6" x2="20" y2="6"/>
                            <line x1="4" y1="12" x2="20" y2="12"/>
                            <line x1="4" y1="18" x2="20" y2="18"/>
                            <circle cx="8" cy="6" r="2" fill="currentColor"/>
                            <circle cx="16" cy="12" r="2" fill="currentColor"/>
                            <circle cx="12" cy="18" r="2" fill="currentColor"/>
                        </svg>
                    </button>
                </div>

                <div class="places-list" id="placesList">
                    <!-- Places will be dynamically inserted here -->
                </div>
            </aside>
        </div>

        <!-- Location Modal -->
        <div class="modal" id="locationModal">
            <div class="modal-overlay" id="modalOverlay"></div>
            <div class="modal-content location-modal-content">
                <button class="modal-close" id="modalClose">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                </button>
                
                <div class="location-header">
                    <div class="location-image" id="locationImage"></div>
                    <div class="location-info">
                        <h2 class="location-name" id="locationName">Restaurant Name</h2>
                        <div class="location-meta">
                            <div class="rating-display">
                                <div class="stars" id="locationStars"></div>
                                <span class="rating-value" id="locationRating">4.2</span>
                            </div>
                            <span class="location-distance" id="locationDistance">0.02 miles</span>
                        </div>
                        <p class="location-description" id="locationDescription">
                            Quick bites • American • Campus dining
                        </p>
                    </div>
                </div>

                <div class="top-reviews" id="topReviews">
                    <h3 class="section-title">Top Reviews</h3>
                    <!-- Top 3 reviews will be inserted here -->
                </div>

                <div class="modal-actions">
                    <button class="btn btn-secondary" id="allReviewsBtn">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                        </svg>
                        View All Reviews
                    </button>
                    <button class="btn btn-primary" id="writeReviewBtn">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M12 20h9"/>
                            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
                        </svg>
                        Write Review
                    </button>
                    <button class="btn btn-accent" id="directionsBtn">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <line x1="5" y1="12" x2="19" y2="12"/>
                            <polyline points="12 5 19 12 12 19"/>
                        </svg>
                        Get Directions
                    </button>
                </div>
            </div>
        </div>

        <!-- Reviews Modal -->
        <div class="modal" id="reviewsModal">
            <div class="modal-overlay" id="reviewsModalOverlay"></div>
            <div class="modal-content reviews-modal-content">
                <button class="modal-close" id="reviewsModalClose">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                </button>
                
                <div class="reviews-header">
                    <div class="reviews-header-content">
                        <h2 class="reviews-title" id="reviewsLocationName">Restaurant Name</h2>
                        <div class="reviews-stats">
                            <div class="rating-large">
                                <span class="rating-number" id="reviewsRating">4.2</span>
                                <div class="stars-large" id="reviewsStars"></div>
                                <span class="reviews-count" id="reviewsCount">128 reviews</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="reviews-controls">
                    <div class="sort-filter-row">
                        <select class="filter-select" id="reviewsSortSelect">
                            <option value="recent">Most Recent</option>
                            <option value="rating-high">Highest Rated</option>
                            <option value="rating-low">Lowest Rated</option>
                            <option value="helpful">Most Helpful</option>
                        </select>
                        <div class="tag-filters" id="tagFilters">
                            <!-- Tag filters will be inserted dynamically -->
                        </div>
                    </div>
                </div>

                <div class="reviews-list" id="reviewsList">
                    <!-- Reviews will be dynamically inserted here -->
                </div>

                <div class="pagination" id="reviewsPagination">
                    <button class="pagination-btn" id="prevPage" disabled>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <polyline points="15 18 9 12 15 6"/>
                        </svg>
                        Previous
                    </button>
                    <div class="pagination-info" id="paginationInfo">
                        Page <span id="currentPage">1</span> of <span id="totalPages">5</span>
                    </div>
                    <button class="pagination-btn" id="nextPage">
                        Next
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <polyline points="9 18 15 12 9 6"/>
                        </svg>
                    </button>
                </div>
            </div>
        </div>
        <!-- Write Review Modal -->
        <div class="modal" id="writeReviewModal">
            <div class="modal-overlay" id="writeReviewModalOverlay"></div>
            <div class="modal-content write-review-modal-content">
                <button class="modal-close" id="writeReviewModalClose">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                </button>
                
                <h2 class="modal-title">Write a Review</h2>
                <p class="modal-subtitle" id="writeReviewLocationName">Restaurant Name</p>

                <form class="review-form" id="reviewForm">
                    <div class="form-group">
                        <label class="form-label">Your Rating</label>
                        <div class="star-input" id="starInput">
                            <button type="button" class="star-btn" data-value="1">★</button>
                            <button type="button" class="star-btn" data-value="2">★</button>
                            <button type="button" class="star-btn" data-value="3">★</button>
                            <button type="button" class="star-btn" data-value="4">★</button>
                            <button type="button" class="star-btn" data-value="5">★</button>
                        </div>
                        <input type="hidden" id="ratingInput" name="rating" value="0" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="reviewTitle">Review Title</label>
                        <input type="text" id="reviewTitle" name="title" class="form-input" placeholder="Sum up your experience" required maxlength="100">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="reviewBody">Your Review</label>
                        <textarea id="reviewBody" name="body" class="form-textarea" placeholder="Share details about your experience..." required minlength="50" maxlength="1000"></textarea>
                        <span class="char-count"><span id="charCount">0</span> / 1000</span>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Tags (optional)</label>
                        <div class="tag-input">
                            <button type="button" class="tag-btn" data-tag="vegan">🌱 Vegan</button>
                            <button type="button" class="tag-btn" data-tag="cheap">💰 Affordable</button>
                            <button type="button" class="tag-btn" data-tag="quick">⚡ Quick Service</button>
                            <button type="button" class="tag-btn" data-tag="healthy">🥗 Healthy</button>
                            <button type="button" class="tag-btn" data-tag="spicy">🌶️ Spicy</button>
                            <button type="button" class="tag-btn" data-tag="late-night">🌙 Late Night</button>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="button" class="btn btn-secondary" id="cancelReviewBtn">Cancel</button>
                        <button type="submit" class="btn btn-primary">Submit Review</button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <!-- Loading Overlay -->
    <div class="loading-overlay" id="loadingOverlay">
        <div class="spinner"></div>
    </div>

    <script src="${pageContext.request.contextPath}/js/auth.js"></script>
    <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>