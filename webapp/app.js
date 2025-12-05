// USC Food Locator Application
// Main JavaScript file for static map interaction, modals, and review system

// Configuration
const CONFIG = {
    USC_CENTER: [34.0224, -118.2851], // USC campus coordinates
    USC_VILLAGE: [34.0250, -118.2850], // USC Village coordinates
    MAP_BOUNDS: {
        north: 34.0280,
        south: 34.0200,
        east: -118.2820,
        west: -118.2880
    },
    API_BASE_URL: '/api', // Update this with your actual API endpoint
    REVIEWS_PER_PAGE: 10
};

// Global state
let mapContainer;
let userMarker;
let locationMarkers = [];
let currentLocation = null;
let currentUserPosition = CONFIG.USC_CENTER;
let allPlaces = [];
let currentReviews = [];
let currentPage = 1;
let totalPages = 1;
let selectedTags = [];

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    initStaticMap();
    loadPlaces();
    setupEventListeners();
    animatePlaceCards();
    checkLoginStatus(); // Check if user is already logged in
});

// Static Map Initialization
function initStaticMap() {
    mapContainer = document.getElementById('map');
    
    // Add user location marker
    addUserMarker(currentUserPosition);
}

// Convert lat/lng to pixel coordinates on the static map
function latLngToPixel(lat, lng) {
    const mapWidth = mapContainer.offsetWidth;
    const mapHeight = mapContainer.offsetHeight;
    
    // Calculate position as percentage of map bounds
    const x = ((lng - CONFIG.MAP_BOUNDS.west) / (CONFIG.MAP_BOUNDS.east - CONFIG.MAP_BOUNDS.west)) * mapWidth;
    const y = ((CONFIG.MAP_BOUNDS.north - lat) / (CONFIG.MAP_BOUNDS.north - CONFIG.MAP_BOUNDS.south)) * mapHeight;
    
    return { x, y };
}

// Add user marker to static map
function addUserMarker(position) {
    const coords = latLngToPixel(position[0], position[1]);
    
    // Remove existing user marker if any
    const existing = mapContainer.querySelector('.user-marker');
    if (existing) existing.remove();
    
    const marker = document.createElement('div');
    marker.className = 'user-marker';
    marker.style.left = `${coords.x}px`;
    marker.style.top = `${coords.y}px`;
    marker.style.transform = 'translate(-50%, -50%)';
    
    mapContainer.appendChild(marker);
    userMarker = marker;
}

// Add place marker to static map
function addPlaceMarker(place) {
    const coords = latLngToPixel(place.lat, place.lng);
    
    const marker = document.createElement('div');
    marker.className = 'map-marker';
    marker.style.left = `${coords.x}px`;
    marker.style.top = `${coords.y}px`;
    marker.style.transform = 'translate(-50%, -100%)'; // Center and align to bottom
    marker.dataset.placeId = place.id;
    marker.title = place.name;
    
    marker.addEventListener('click', () => showLocationModal(place));
    
    mapContainer.appendChild(marker);
    locationMarkers.push(marker);
}

// Clear all place markers
function clearPlaceMarkers() {
    locationMarkers.forEach(marker => marker.remove());
    locationMarkers = [];
}

// Load places from backend
async function loadPlaces() {
    try {
        showLoading(true);
        
        // Mock data for demonstration - replace with actual API call
        // const response = await fetch(`${CONFIG.API_BASE_URL}/locations`);
        // const data = await response.json();
        
        // Mock places data
        allPlaces = generateMockPlaces();
        
        displayPlaces(allPlaces);
        addPlaceMarkers(allPlaces);
        
        showLoading(false);
    } catch (error) {
        console.error('Error loading places:', error);
        showLoading(false);
        showError('Failed to load locations. Please try again.');
    }
}

// Generate mock places for demonstration
function generateMockPlaces() {
    const mockPlaces = [
        {
            id: 1,
            name: 'Dulce',
            lat: 34.0250,
            lng: -118.2850,
            rating: 4.2,
            description: 'Quick bites • Desserts • Campus dining',
            tags: ['quick', 'cheap'],
            distance: 0.02
        },
        {
            id: 2,
            name: 'Everybody\'s Kitchen',
            lat: 34.0245,
            lng: -118.2855,
            rating: 4.0,
            description: 'International • Healthy options • Dining hall',
            tags: ['healthy', 'vegan'],
            distance: 0.8
        },
        {
            id: 3,
            name: 'Lemonade',
            lat: 34.0265,
            lng: -118.2845,
            rating: 3.7,
            description: 'Healthy • Fresh • Salads',
            tags: ['healthy', 'vegan', 'quick'],
            distance: 1.3
        },
        {
            id: 4,
            name: 'Seeds Marketplace',
            lat: 34.0230,
            lng: -118.2865,
            rating: 4.5,
            description: 'Quick service • Coffee • Sandwiches',
            tags: ['quick', 'cheap'],
            distance: 0.15
        },
        {
            id: 5,
            name: 'Popchew',
            lat: 34.0240,
            lng: -118.2840,
            rating: 4.3,
            description: 'Bubble tea • Snacks • Asian',
            tags: ['quick', 'cheap'],
            distance: 0.25
        }
    ];
    
    return mockPlaces;
}

// Display places in sidebar
function displayPlaces(places) {
    const placesList = document.getElementById('placesList');
    
    // Sort places by distance by default
    const sortedPlaces = [...places].sort((a, b) => a.distance - b.distance);
    
    placesList.innerHTML = sortedPlaces.map((place, index) => `
        <div class="place-card" data-place-id="${place.id}" style="animation-delay: ${index * 0.05}s;">
            <div class="place-card-header">
                <div>
                    <h3 class="place-name">${place.name}</h3>
                    <div class="place-rating">
                        <div class="stars">
                            ${generateStars(place.rating)}
                        </div>
                        <span class="rating-value">${place.rating.toFixed(1)}</span>
                    </div>
                </div>
                <span class="place-distance">${place.distance} mi</span>
            </div>
            <div class="place-tags">
                ${place.tags.map(tag => `<span class="tag">${getTagIcon(tag)} ${tag}</span>`).join('')}
            </div>
        </div>
    `).join('');
    
    // Add click listeners to place cards
    document.querySelectorAll('.place-card').forEach(card => {
        card.addEventListener('click', () => {
            const placeId = parseInt(card.dataset.placeId);
            const place = places.find(p => p.id === placeId);
            showLocationModal(place);
        });
    });
}

// Add place markers to map
function addPlaceMarkers(places) {
    // Clear existing markers
    clearPlaceMarkers();
    
    places.forEach(place => {
        addPlaceMarker(place);
    });
}

// Generate star rating HTML
function generateStars(rating) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    
    let starsHtml = '';
    for (let i = 0; i < fullStars; i++) {
        starsHtml += '<span class="star">★</span>';
    }
    if (hasHalfStar) {
        starsHtml += '<span class="star">★</span>';
    }
    for (let i = 0; i < emptyStars; i++) {
        starsHtml += '<span class="star empty">★</span>';
    }
    
    return starsHtml;
}

// Get tag icon
function getTagIcon(tag) {
    const icons = {
        'vegan': '🌱',
        'cheap': '💰',
        'quick': '⚡',
        'healthy': '🥗',
        'spicy': '🌶️',
        'late-night': '🌙'
    };
    return icons[tag] || '•';
}

// Show location modal
function showLocationModal(place) {
    currentLocation = place;
    const modal = document.getElementById('locationModal');
    
    // Update modal content
    document.getElementById('locationName').textContent = place.name;
    document.getElementById('locationRating').textContent = place.rating.toFixed(1);
    document.getElementById('locationStars').innerHTML = generateStars(place.rating);
    document.getElementById('locationDistance').textContent = `${place.distance} miles`;
    document.getElementById('locationDescription').textContent = place.description;
    
    // Set location image (placeholder)
    const locationImage = document.getElementById('locationImage');
    locationImage.innerHTML = getPlaceEmoji(place.name);
    
    // Load and display top reviews
    loadTopReviews(place.id);
    
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

// Get place emoji based on name/type
function getPlaceEmoji(name) {
    const emojis = {
        'dulce': '🍰',
        'kitchen': '🍽️',
        'lemonade': '🥗',
        'seeds': '☕',
        'popchew': '🧋'
    };
    
    const key = Object.keys(emojis).find(k => name.toLowerCase().includes(k));
    return emojis[key] || '🍴';
}

// Load top reviews for a location
async function loadTopReviews(locationId) {
    try {
        // Mock reviews - replace with actual API call
        // const response = await fetch(`${CONFIG.API_BASE_URL}/reviews/${locationId}/top`);
        // const reviews = await response.json();
        
        const reviews = generateMockReviews(locationId, 3);
        displayTopReviews(reviews);
    } catch (error) {
        console.error('Error loading reviews:', error);
    }
}

// Generate mock reviews
function generateMockReviews(locationId, count = 10) {
    const authors = ['Sarah K.', 'Mike T.', 'Emma L.', 'John D.', 'Lisa W.', 'Chris P.', 'Amy R.', 'David M.'];
    const titles = [
        'Great food and quick service!',
        'Perfect spot for lunch',
        'Could be better',
        'Amazing experience',
        'Decent but overpriced',
        'Hidden gem on campus',
        'Not worth the hype',
        'My go-to spot'
    ];
    const bodies = [
        'The food here is consistently good. I come here at least twice a week and have never been disappointed.',
        'Quick service and great taste. Perfect for when you\'re in a hurry between classes.',
        'It\'s okay, nothing special. The portions could be bigger for the price.',
        'Absolutely love this place! The atmosphere is great and the food is even better.',
        'Food is decent but a bit overpriced for what you get. Still worth trying though.',
        'This has become my favorite spot on campus. The staff is friendly and the food is always fresh.',
        'I had high expectations based on reviews but was somewhat disappointed. Maybe I ordered the wrong thing.',
        'Can\'t get enough of this place. The variety is great and everything I\'ve tried has been delicious.'
    ];
    const tags = [['vegan', 'healthy'], ['quick', 'cheap'], ['spicy'], ['healthy'], ['vegan'], ['late-night', 'cheap']];
    
    const reviews = [];
    for (let i = 0; i < count; i++) {
        reviews.push({
            id: i + 1,
            locationId,
            author: authors[Math.floor(Math.random() * authors.length)],
            rating: Math.random() * 2 + 3, // 3-5 stars
            title: titles[Math.floor(Math.random() * titles.length)],
            body: bodies[Math.floor(Math.random() * bodies.length)],
            tags: tags[Math.floor(Math.random() * tags.length)],
            createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000), // Random date within last 30 days
            helpfulCount: Math.floor(Math.random() * 50)
        });
    }
    
    return reviews.sort((a, b) => b.rating - a.rating);
}

// Display top reviews in location modal
function displayTopReviews(reviews) {
    const topReviewsContainer = document.getElementById('topReviews');
    const reviewsHtml = reviews.map(review => `
        <div class="review-preview">
            <div class="review-header">
                <span class="review-author">${review.author}</span>
                <span class="review-date">${formatDate(review.createdAt)}</span>
            </div>
            <div class="review-rating">
                ${generateStars(review.rating)}
            </div>
            <h4 class="review-title">${review.title}</h4>
            <p class="review-body">${truncateText(review.body, 120)}</p>
        </div>
    `).join('');
    
    topReviewsContainer.innerHTML = `
        <h3 class="section-title">Top Reviews</h3>
        ${reviewsHtml}
    `;
}

// Show all reviews modal
function showReviewsModal() {
    if (!currentLocation) return;
    
    const modal = document.getElementById('reviewsModal');
    document.getElementById('reviewsLocationName').textContent = currentLocation.name;
    document.getElementById('reviewsRating').textContent = currentLocation.rating.toFixed(1);
    document.getElementById('reviewsStars').innerHTML = generateStars(currentLocation.rating);
    
    // Load all reviews
    loadAllReviews(currentLocation.id);
    
    // Setup tag filters
    setupTagFilters();
    
    modal.classList.add('active');
}

// Load all reviews with pagination
async function loadAllReviews(locationId, page = 1, sortBy = 'recent', filters = []) {
    try {
        showLoading(true);
        
        // Mock reviews - replace with actual API call
        const allReviews = generateMockReviews(locationId, 45);
        
        // Apply sorting
        let sortedReviews = [...allReviews];
        switch (sortBy) {
            case 'rating-high':
                sortedReviews.sort((a, b) => b.rating - a.rating);
                break;
            case 'rating-low':
                sortedReviews.sort((a, b) => a.rating - b.rating);
                break;
            case 'helpful':
                sortedReviews.sort((a, b) => b.helpfulCount - a.helpfulCount);
                break;
            default: // recent
                sortedReviews.sort((a, b) => b.createdAt - a.createdAt);
        }
        
        // Apply tag filters
        if (filters.length > 0) {
            sortedReviews = sortedReviews.filter(review =>
                filters.some(tag => review.tags.includes(tag))
            );
        }
        
        currentReviews = sortedReviews;
        totalPages = Math.ceil(sortedReviews.length / CONFIG.REVIEWS_PER_PAGE);
        currentPage = page;
        
        // Get reviews for current page
        const startIndex = (page - 1) * CONFIG.REVIEWS_PER_PAGE;
        const endIndex = startIndex + CONFIG.REVIEWS_PER_PAGE;
        const pageReviews = sortedReviews.slice(startIndex, endIndex);
        
        displayAllReviews(pageReviews);
        updatePagination();
        updateReviewsCount(sortedReviews.length);
        
        showLoading(false);
    } catch (error) {
        console.error('Error loading reviews:', error);
        showLoading(false);
    }
}

// Display all reviews
function displayAllReviews(reviews) {
    const reviewsList = document.getElementById('reviewsList');
    
    reviewsList.innerHTML = reviews.map(review => `
        <div class="review-card">
            <div class="review-card-header">
                <div class="review-author-info">
                    <div class="review-avatar">${review.author[0]}</div>
                    <div class="review-author-details">
                        <span class="review-author">${review.author}</span>
                        <div class="review-meta">
                            <span class="review-date">${formatDate(review.createdAt)}</span>
                        </div>
                    </div>
                </div>
                <div class="review-actions">
                    <button class="helpful-btn" data-review-id="${review.id}">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
                        </svg>
                        <span>${review.helpfulCount}</span>
                    </button>
                </div>
            </div>
            <div class="review-rating">
                ${generateStars(review.rating)}
            </div>
            ${review.tags.length > 0 ? `
                <div class="review-tags">
                    ${review.tags.map(tag => `<span class="review-tag">${getTagIcon(tag)} ${tag}</span>`).join('')}
                </div>
            ` : ''}
            <h4 class="review-title">${review.title}</h4>
            <p class="review-body">${review.body}</p>
        </div>
    `).join('');
    
    // Add helpful button listeners
    document.querySelectorAll('.helpful-btn').forEach(btn => {
        btn.addEventListener('click', handleHelpfulClick);
    });
}

// Setup tag filters
function setupTagFilters() {
    const allTags = ['vegan', 'cheap', 'quick', 'healthy', 'spicy', 'late-night'];
    const tagFiltersContainer = document.getElementById('tagFilters');
    
    tagFiltersContainer.innerHTML = allTags.map(tag => `
        <button class="tag-filter" data-tag="${tag}">
            ${getTagIcon(tag)} ${tag}
        </button>
    `).join('');
    
    // Add click listeners
    document.querySelectorAll('.tag-filter').forEach(btn => {
        btn.addEventListener('click', () => {
            btn.classList.toggle('active');
            const tag = btn.dataset.tag;
            
            if (selectedTags.includes(tag)) {
                selectedTags = selectedTags.filter(t => t !== tag);
            } else {
                selectedTags.push(tag);
            }
            
            // Reload reviews with filters
            const sortBy = document.getElementById('reviewsSortSelect').value;
            loadAllReviews(currentLocation.id, 1, sortBy, selectedTags);
        });
    });
}

// Handle helpful button click
function handleHelpfulClick(e) {
    const btn = e.currentTarget;
    const reviewId = btn.dataset.reviewId;
    
    // Toggle active state
    btn.classList.toggle('active');
    
    // Update count (mock - would be API call in production)
    const countSpan = btn.querySelector('span');
    let count = parseInt(countSpan.textContent);
    count = btn.classList.contains('active') ? count + 1 : count - 1;
    countSpan.textContent = count;
    
    // In production, send to API
    // updateReviewHelpful(reviewId, btn.classList.contains('active'));
}

// Update pagination
function updatePagination() {
    document.getElementById('currentPage').textContent = currentPage;
    document.getElementById('totalPages').textContent = totalPages;
    
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    prevBtn.disabled = currentPage === 1;
    nextBtn.disabled = currentPage === totalPages;
}

// Update reviews count
function updateReviewsCount(count) {
    document.getElementById('reviewsCount').textContent = `${count} reviews`;
}

// Show write review modal
function showWriteReviewModal() {
    if (!currentLocation) return;
    
    const modal = document.getElementById('writeReviewModal');
    document.getElementById('writeReviewLocationName').textContent = currentLocation.name;
    
    // Reset form
    document.getElementById('reviewForm').reset();
    resetStarInput();
    resetTagInput();
    
    modal.classList.add('active');
}

// Reset star input
function resetStarInput() {
    document.querySelectorAll('.star-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.getElementById('ratingInput').value = '0';
}

// Reset tag input
function resetTagInput() {
    document.querySelectorAll('.tag-btn').forEach(btn => {
        btn.classList.remove('active');
    });
}

// Handle review form submission
async function handleReviewSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const rating = formData.get('rating');
    const title = formData.get('title');
    const body = formData.get('body');
    
    // Get selected tags
    const tags = [];
    document.querySelectorAll('.tag-btn.active').forEach(btn => {
        tags.push(btn.dataset.tag);
    });
    
    // Validate
    if (rating === '0') {
        alert('Please select a rating');
        return;
    }
    
    try {
        showLoading(true);
        
        // Mock API call - replace with actual endpoint
        const reviewData = {
            locationId: currentLocation.id,
            rating: parseFloat(rating),
            title,
            body,
            tags,
            createdAt: new Date()
        };
        
        // const response = await fetch(`${CONFIG.API_BASE_URL}/reviews`, {
        //     method: 'POST',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(reviewData)
        // });
        
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        showLoading(false);
        closeModal('writeReviewModal');
        
        // Show success message
        alert('Review submitted successfully!');
        
        // Reload reviews
        loadTopReviews(currentLocation.id);
    } catch (error) {
        console.error('Error submitting review:', error);
        showLoading(false);
        alert('Failed to submit review. Please try again.');
    }
}

// Geolocation
function getUserLocation() {
    if ('geolocation' in navigator) {
        showLoading(true);
        
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                currentUserPosition = [latitude, longitude];
                
                // Update user marker on static map
                addUserMarker(currentUserPosition);
                
                // Update distance indicator
                document.getElementById('userDistanceIndicator').innerHTML = `
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <circle cx="12" cy="12" r="10"/>
                        <circle cx="12" cy="12" r="3" fill="currentColor"/>
                    </svg>
                    <span>Your Location</span>
                `;
                
                // Recalculate distances and update places
                updatePlacesDistances();
                
                showLoading(false);
            },
            (error) => {
                console.error('Geolocation error:', error);
                showLoading(false);
                alert('Unable to get your location. Using USC campus as default.');
            }
        );
    } else {
        alert('Geolocation is not supported by your browser.');
    }
}

// Update places distances based on user location
function updatePlacesDistances() {
    allPlaces.forEach(place => {
        place.distance = calculateDistance(
            currentUserPosition[0],
            currentUserPosition[1],
            place.lat,
            place.lng
        );
    });
    
    displayPlaces(allPlaces);
}

// Calculate distance between two coordinates (Haversine formula)
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 3959; // Earth's radius in miles
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    
    return Math.round(distance * 100) / 100; // Round to 2 decimal places
}

function toRad(degrees) {
    return degrees * (Math.PI / 180);
}

// Get directions
function getDirections() {
    if (!currentLocation) return;
    
    const url = `https://www.google.com/maps/dir/?api=1&destination=${currentLocation.lat},${currentLocation.lng}`;
    window.open(url, '_blank');
}

// Login/Register Functions
function showLoginModal() {
    const modal = document.getElementById('loginModal');
    showLoginForm();
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function showLoginForm() {
    document.getElementById('loginContainer').style.display = 'block';
    document.getElementById('registerContainer').style.display = 'none';
    document.getElementById('loginForm').reset();
}

function showRegisterForm() {
    document.getElementById('loginContainer').style.display = 'none';
    document.getElementById('registerContainer').style.display = 'block';
    document.getElementById('registerForm').reset();
}

async function handleLogin(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const username = formData.get('username');
    const password = formData.get('password');
    const rememberMe = formData.get('rememberMe') === 'on';
    
    try {
        showLoading(true);
        
        // Mock API call - replace with actual endpoint
        const loginData = { username, password, rememberMe };
        
        // const response = await fetch(`${CONFIG.API_BASE_URL}/auth/login`, {
        //     method: 'POST',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(loginData)
        // });
        // const data = await response.json();
        
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // Mock successful login
        const mockUser = {
            userID: 1,
            username: username,
            firstName: 'John',
            lastName: 'Doe'
        };
        
        // Store user in sessionStorage
        sessionStorage.setItem('user', JSON.stringify(mockUser));
        
        showLoading(false);
        closeModal('loginModal');
        
        // Update UI to show logged in state
        updateLoginButton(mockUser);
        
        alert('Login successful!');
    } catch (error) {
        console.error('Error logging in:', error);
        showLoading(false);
        alert('Login failed. Please try again.');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const firstName = formData.get('firstName');
    const lastName = formData.get('lastName');
    const email = formData.get('email');
    const username = formData.get('username');
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    
    // Validate passwords match
    if (password !== confirmPassword) {
        alert('Passwords do not match!');
        return;
    }
    
    // Validate password length
    if (password.length < 8) {
        alert('Password must be at least 8 characters long!');
        return;
    }
    
    try {
        showLoading(true);
        
        // Mock API call - replace with actual endpoint
        const registerData = { firstName, lastName, email, username, password };
        
        // const response = await fetch(`${CONFIG.API_BASE_URL}/auth/register`, {
        //     method: 'POST',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(registerData)
        // });
        // const data = await response.json();
        
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        showLoading(false);
        
        // Show success and switch to login
        alert('Registration successful! Please log in.');
        showLoginForm();
    } catch (error) {
        console.error('Error registering:', error);
        showLoading(false);
        alert('Registration failed. Please try again.');
    }
}

function updateLoginButton(user) {
    const loginBtn = document.getElementById('loginBtn');
    
    if (user) {
        loginBtn.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
            </svg>
            ${user.firstName || user.username}
        `;
        
        // Update click handler to show profile/logout
        loginBtn.onclick = () => {
            const action = confirm('Log out?');
            if (action) {
                logout();
            }
        };
    } else {
        loginBtn.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
            </svg>
            Login
        `;
        loginBtn.onclick = showLoginModal;
    }
}

function logout() {
    sessionStorage.removeItem('user');
    updateLoginButton(null);
    alert('Logged out successfully!');
}

function checkLoginStatus() {
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
        const user = JSON.parse(userStr);
        updateLoginButton(user);
    }
}

// Utility functions
function formatDate(date) {
    const now = new Date();
    const reviewDate = new Date(date);
    const diffTime = Math.abs(now - reviewDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
    if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
    return `${Math.floor(diffDays / 365)} years ago`;
}

function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substr(0, maxLength) + '...';
}

function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (show) {
        overlay.classList.add('active');
    } else {
        overlay.classList.remove('active');
    }
}

function showError(message) {
    alert(message); // In production, use a better error notification system
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('active');
    document.body.style.overflow = '';
}

function animatePlaceCards() {
    const cards = document.querySelectorAll('.place-card');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.05}s`;
    });
}

// Event Listeners Setup
function setupEventListeners() {
    // Locate button
    document.getElementById('locateBtn').addEventListener('click', getUserLocation);
    
    // Login button
    document.getElementById('loginBtn').addEventListener('click', showLoginModal);
    
    // Sort select
    document.getElementById('sortSelect').addEventListener('change', (e) => {
        const sortBy = e.target.value;
        let sortedPlaces = [...allPlaces];
        
        switch (sortBy) {
            case 'distance':
                sortedPlaces.sort((a, b) => a.distance - b.distance);
                break;
            case 'rating':
                sortedPlaces.sort((a, b) => b.rating - a.rating);
                break;
            case 'popular':
                // Sort by some popularity metric (mock)
                sortedPlaces.sort((a, b) => b.rating - a.rating);
                break;
        }
        
        displayPlaces(sortedPlaces);
    });
    
    // Location modal
    document.getElementById('modalClose').addEventListener('click', () => closeModal('locationModal'));
    document.getElementById('modalOverlay').addEventListener('click', () => closeModal('locationModal'));
    document.getElementById('allReviewsBtn').addEventListener('click', () => {
        closeModal('locationModal');
        showReviewsModal();
    });
    document.getElementById('writeReviewBtn').addEventListener('click', () => {
        closeModal('locationModal');
        showWriteReviewModal();
    });
    document.getElementById('directionsBtn').addEventListener('click', getDirections);
    
    // Reviews modal
    document.getElementById('reviewsModalClose').addEventListener('click', () => closeModal('reviewsModal'));
    document.getElementById('reviewsModalOverlay').addEventListener('click', () => closeModal('reviewsModal'));
    document.getElementById('reviewsSortSelect').addEventListener('change', (e) => {
        loadAllReviews(currentLocation.id, 1, e.target.value, selectedTags);
    });
    
    // Pagination
    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 1) {
            const sortBy = document.getElementById('reviewsSortSelect').value;
            loadAllReviews(currentLocation.id, currentPage - 1, sortBy, selectedTags);
        }
    });
    document.getElementById('nextPage').addEventListener('click', () => {
        if (currentPage < totalPages) {
            const sortBy = document.getElementById('reviewsSortSelect').value;
            loadAllReviews(currentLocation.id, currentPage + 1, sortBy, selectedTags);
        }
    });
    
    // Login modal
    document.getElementById('loginModalClose').addEventListener('click', () => closeModal('loginModal'));
    document.getElementById('loginModalOverlay').addEventListener('click', () => closeModal('loginModal'));
    document.getElementById('showRegisterBtn').addEventListener('click', showRegisterForm);
    document.getElementById('showLoginBtn').addEventListener('click', showLoginForm);
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // Write review modal
    document.getElementById('writeReviewModalClose').addEventListener('click', () => closeModal('writeReviewModal'));
    document.getElementById('writeReviewModalOverlay').addEventListener('click', () => closeModal('writeReviewModal'));
    document.getElementById('cancelReviewBtn').addEventListener('click', () => closeModal('writeReviewModal'));
    
    // Star input
    document.querySelectorAll('.star-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const value = parseInt(btn.dataset.value);
            document.getElementById('ratingInput').value = value;
            
            // Update visual state
            document.querySelectorAll('.star-btn').forEach((star, index) => {
                if (index < value) {
                    star.classList.add('active');
                } else {
                    star.classList.remove('active');
                }
            });
        });
    });
    
    // Tag input
    document.querySelectorAll('.tag-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            btn.classList.toggle('active');
        });
    });
    
    // Character count
    const reviewBody = document.getElementById('reviewBody');
    const charCount = document.getElementById('charCount');
    reviewBody.addEventListener('input', () => {
        charCount.textContent = reviewBody.value.length;
    });
    
    // Review form submission
    document.getElementById('reviewForm').addEventListener('submit', handleReviewSubmit);
    
    // Close modals on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeModal('locationModal');
            closeModal('reviewsModal');
            closeModal('writeReviewModal');
            closeModal('loginModal');
        }
    });
}

// Export for testing (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        calculateDistance,
        formatDate,
        truncateText,
        generateStars
    };
}