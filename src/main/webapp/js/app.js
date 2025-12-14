// USC Food Locator - SIMPLIFIED VERSION
// Only essential functionality, no dead code

const CONFIG = {
    USC_CENTER: [34.0224, -118.2851],
    MAP_BOUNDS: {
        north: 34.0281,
        south: 34.0155,
        east: -118.2712,
        west: -118.2973
    },
    API_BASE_URL: '/foodlocator/api'
};

// Add this variable at the top of app.js with other declarations
let currentUserPosition = null;

let mapContainer;
let currentLocation = null;
let allPlaces = [];
let locationMarkers = [];
let currentReviews = [];
let currentPage = 1;
let totalReviews = 0;
let totalPages = 1;

console.log('mapContainer:', mapContainer);

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    initStaticMap();
    loadPlaces();
    setupEventListeners();
});

// Map initialization
function initStaticMap() {
    mapContainer = document.getElementById('map');
    if (!mapContainer) console.error('Map container not found');
}

function latLngToPixel(lat, lng) {
    const mapWidth = mapContainer.offsetWidth;
    const mapHeight = mapContainer.offsetHeight;
    const x = ((lng - CONFIG.MAP_BOUNDS.west) / (CONFIG.MAP_BOUNDS.east - CONFIG.MAP_BOUNDS.west)) * mapWidth;
    const y = ((CONFIG.MAP_BOUNDS.north - lat) / (CONFIG.MAP_BOUNDS.north - CONFIG.MAP_BOUNDS.south)) * mapHeight;
    return { x, y };
}

// Load places from API
async function loadPlaces() {
    try {
        showLoading(true);
        const response = await fetch(`${CONFIG.API_BASE_URL}/locations`, { credentials: 'include' });
        const data = await response.json();
        
        if (data.success && Array.isArray(data.data)) {
            allPlaces = data.data.map(loc => ({
                id: loc.locationID,
                name: loc.name,
                lat: loc.lat,
                lng: loc.lng,
                rating: loc.rating || 0,
                description: loc.description || loc.category
            }));
            displayPlaces(allPlaces);
            addPlaceMarkers(allPlaces);
        } else {
            showError('Failed to load locations');
        }
        showLoading(false);
    } catch (error) {
        console.error('Error loading places:', error);
        showLoading(false);
        showError('Error loading locations');
    }
}

function expandMapBounds(userLat, userLng) {
    // Check if user is outside current bounds
    const isOutside = userLat > CONFIG.MAP_BOUNDS.north || 
                      userLat < CONFIG.MAP_BOUNDS.south ||
                      userLng < CONFIG.MAP_BOUNDS.west ||
                      userLng > CONFIG.MAP_BOUNDS.east;
    
    if (isOutside) {
        // Expand bounds to include user location with padding
        const padding = 0.01; // ~0.6 miles padding
        
        CONFIG.MAP_BOUNDS.north = Math.max(CONFIG.MAP_BOUNDS.north, userLat + padding);
        CONFIG.MAP_BOUNDS.south = Math.min(CONFIG.MAP_BOUNDS.south, userLat - padding);
        CONFIG.MAP_BOUNDS.east = Math.max(CONFIG.MAP_BOUNDS.east, userLng + padding);
        CONFIG.MAP_BOUNDS.west = Math.min(CONFIG.MAP_BOUNDS.west, userLng - padding);
        
        // Redraw all markers with new bounds
        redrawAllMarkers();
    }
}

function redrawAllMarkers() {
    // Clear existing markers
    clearPlaceMarkers();
    
    // Re-add place markers with new coordinate system
    addPlaceMarkers(allPlaces);
    
    // Re-add user marker if it exists
    if (currentUserPosition) {
        addUserMarker(currentUserPosition);
    }
}

// Display places in sidebar
function displayPlaces(places) {
    const placesList = document.getElementById('placesList');
    if (!placesList) return;
    
    placesList.innerHTML = places.map((place, idx) => `
        <div class="place-card" data-place-id="${place.id}">
            <div class="place-card-header">
                <h3>${place.name}</h3>
                <div class="place-rating">
                    <span class="stars">${generateStars(place.rating)}</span>
                    <span>${place.rating.toFixed(1)}</span>
                </div>
            </div>
            <p class="place-description">${place.description}</p>
            ${place.distance !== undefined ? `<p class="place-distance" style="font-size: 12px; color: #666; margin-top: 4px;">📍 ${place.distance} mi</p>` : ''}
        </div>
    `).join('');
    
    // Add click listeners
    document.querySelectorAll('.place-card').forEach(card => {
        card.addEventListener('click', () => {
            const placeId = card.dataset.placeId;
            const place = allPlaces.find(p => p.id == placeId);
            if (place) showLocationModal(place);
        });
    });
}

function sortPlaces(sortBy) {
    const sorted = [...allPlaces];
    const userLocation = CONFIG.USC_CENTER; // [34.0224, -118.2851]
    
    if (sortBy === 'distance') {
        // Sort by distance from USC Center
        sorted.sort((a, b) => {
            const distA = Math.sqrt(Math.pow(a.lat - userLocation[0], 2) + Math.pow(a.lng - userLocation[1], 2));
            const distB = Math.sqrt(Math.pow(b.lat - userLocation[0], 2) + Math.pow(b.lng - userLocation[1], 2));
            return distA - distB;
        });
    } else if (sortBy === 'rating') {
        // Sort by rating (highest first)
        sorted.sort((a, b) => b.rating - a.rating);
    } else if (sortBy === 'popular') {
        // Sort by number of reviews (popularity)
        sorted.sort((a, b) => b.reviewCount - a.reviewCount);
    }
    
    displayPlaces(sorted);
}

function generateStars(rating) {
    const full = Math.floor(rating);
    let html = '';
    for (let i = 0; i < 5; i++) {
        html += i < full ? '★' : '☆';
    }
    return html;
}

// Add markers to map
function addPlaceMarkers(places) {
    clearPlaceMarkers();
    places.forEach(place => {
        const coords = latLngToPixel(place.lat, place.lng);
        const marker = document.createElement('div');
        marker.className = 'map-marker';
        marker.style.left = `${coords.x}px`;
        marker.style.top = `${coords.y}px`;
        marker.title = place.name;
        marker.addEventListener('click', () => showLocationModal(place));
        mapContainer.appendChild(marker);
        locationMarkers.push(marker);
    });
}

function clearPlaceMarkers() {
    locationMarkers.forEach(m => m.remove());
    locationMarkers = [];
}

// Show location modal
function showLocationModal(place) {
    currentLocation = place;
    document.getElementById('locationName').textContent = place.name;
    document.getElementById('locationRating').textContent = place.rating.toFixed(1);
    document.getElementById('locationDescription').textContent = place.description;
    document.getElementById('locationModal').classList.add('active');
    document.body.style.overflow = 'hidden';
    loadTopReviews(place.id);
}

// Load top 3 reviews
async function loadTopReviews(locationId) {
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/reviews/${locationId}/top`, { credentials: 'include' });
        const result = await response.json();
        const reviews = result.success && result.data ? result.data : [];
        displayTopReviews(reviews);
    } catch (error) {
        console.error('Error loading reviews:', error);
        displayTopReviews([]);
    }
}

function displayTopReviews(reviews) {
    const container = document.getElementById('topReviews');
    if (!container) return;
    
    if (reviews.length === 0) {
        container.innerHTML = '<p class="empty-message">No reviews yet</p>';
        return;
    }
    
    container.innerHTML = reviews.map(r => `
        <div class="review-card">
            <strong>${r.author || 'Anonymous'}</strong>
            <div>${generateStars(r.rating)}</div>
            <h4>${r.title}</h4>
            <p>${r.body.substring(0, 200)}...</p>
            <small>${formatDate(r.createdAt)}</small>
        </div>
    `).join('');
}

// Utility functions
function formatDate(dateStr) {
    try {
        const date = new Date(dateStr);
        const now = new Date();
        const days = Math.floor((now - date) / (1000 * 60 * 60 * 24));
        if (days === 0) return 'Today';
        if (days === 1) return 'Yesterday';
        if (days < 7) return `${days}d ago`;
        if (days < 30) return `${Math.floor(days / 7)}w ago`;
        return `${Math.floor(days / 30)}m ago`;
    } catch { return ''; }
}

function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) overlay.classList.toggle('active', show);
}

function showError(msg) {
    alert(msg);
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
}

// Event listeners
function setupEventListeners() {
    // Close modals
    ['locationModal', 'reviewsModal'].forEach(id => {
        const close = document.getElementById(id + 'Close');
        const overlay = document.getElementById(id + 'Overlay');
        if (close) close.addEventListener('click', () => closeModal(id));
        if (overlay) overlay.addEventListener('click', () => closeModal(id));
    });
    
    // View all reviews button
    const allReviewsBtn = document.getElementById('allReviewsBtn');
    if (allReviewsBtn) {
        allReviewsBtn.addEventListener('click', () => {
            closeModal('locationModal');
            showReviewsModal();
        });
    }
    
    // Write review button
    const writeBtn = document.getElementById('writeReviewBtn');
    if (writeBtn) {
        writeBtn.addEventListener('click', () => {
            if (!window.currentUser) {
                alert('Please login to write a review');
                return;
            }
            const loc = currentLocation;
            window.location.href = `/foodlocator/jsp/write-review.jsp?locationId=${loc.id}&locationName=${encodeURIComponent(loc.name)}`;
        });
    }
    
    // Get Directions button
    const directionsBtn = document.getElementById('directionsBtn');
    if (directionsBtn) {
        directionsBtn.addEventListener('click', () => {
            if (currentLocation) {
                getDirections(currentLocation);
            }
        });
    }
    
    // Sort dropdown for places
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', (e) => {
            sortPlaces(e.target.value);
        });
    }
    
    // Locate Me button
    const locateBtn = document.getElementById('locateBtn');
    if (locateBtn) {
        locateBtn.addEventListener('click', locateMe);
        console.log('Locate Me button listener attached'); // Debug log
    } else {
        console.error('locateBtn element not found!'); // Debug log
    }

    // My Reviews button
    const navBtns = document.querySelectorAll('.nav-btn');
    navBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            navBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            const view = btn.dataset.view;
            if (view === 'my-reviews') {
                showMyReviewsModal();
            }
        });
    });
}

async function showMyReviewsModal() {
    if (!window.currentUser) {
        alert('Please login to view your reviews');
        return;
    }
    
    try {
        showLoading(true);
        
        // Fetch user's reviews from all locations
        const response = await fetch(
            `${CONFIG.API_BASE_URL}/reviews/user/${window.currentUser.userId}`,
            { credentials: 'include' }
        );
        const result = await response.json();
        
        showLoading(false);
        
        if (result.success) {
            displayMyReviews(result.data);
        } else {
            showError('Failed to load your reviews');
        }
    } catch (error) {
        showLoading(false);
        console.error('Error loading user reviews:', error);
        showError('Error loading your reviews');
    }
}

function displayMyReviews(reviews) {
    // Open reviews modal with user's reviews
    document.getElementById('reviewsModal').classList.add('active');
    document.body.style.overflow = 'hidden';
    document.getElementById('reviewsLocationName').textContent = 'My Reviews';
    document.getElementById('reviewsRating').textContent = '';
    document.getElementById('reviewsStars').innerHTML = '';
    document.getElementById('reviewsCount').textContent = `${reviews.length} review${reviews.length !== 1 ? 's' : ''}`;
    
    // Hide sort controls for my reviews
    document.getElementById('reviewsSortSelect').style.display = 'none';
    
    const container = document.getElementById('reviewsList');
    if (!container) return;
    
    if (reviews.length === 0) {
        container.innerHTML = '<p class="empty-message">You haven\'t written any reviews yet</p>';
        return;
    }
    
    container.innerHTML = reviews.map(r => `
        <div class="review-card">
            <div class="review-header">
                <div>
                    <strong>${r.locationName || 'Unknown Location'}</strong>
                    <small>${formatDate(r.createdAt)}</small>
                </div>
                <div>${generateStars(r.rating)}</div>
            </div>
            <h4>${r.title}</h4>
            <p>${r.body}</p>
        </div>
    `).join('');
    
    // Hide pagination for my reviews
    document.getElementById('reviewsPagination').style.display = 'none';
}

function locateMe() {
    if ('geolocation' in navigator) {
        showLoading(true);
        
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                currentUserPosition = [latitude, longitude];
                
                // DON'T expand bounds - just add marker
                addUserMarker(currentUserPosition);
                
                // Update distance indicator in sidebar
                const indicator = document.getElementById('userDistanceIndicator');
                if (indicator) {
                    indicator.innerHTML = `
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <circle cx="12" cy="12" r="10"/>
                            <circle cx="12" cy="12" r="3" fill="currentColor"/>
                        </svg>
                        <span>Your Location</span>
                    `;
                }
                
                // Recalculate distances and update places
                updatePlacesDistances();
                
                showLoading(false);
            },
            (error) => {
                console.error('Geolocation error:', error);
                showLoading(false);
                showError('Unable to get your location. Using USC campus as default.');
            }
        );
    } else {
        showError('Geolocation is not supported by your browser.');
    }
}

function addUserMarker(position) {
    const coords = latLngToPixel(position[0], position[1]);
    
    // Get map dimensions
    const mapWidth = mapContainer.offsetWidth;
    const mapHeight = mapContainer.offsetHeight;
    
    // Clamp coordinates to map boundaries (with small margin)
    const margin = 10; // 10px from edge
    const clampedX = Math.max(margin, Math.min(coords.x, mapWidth - margin));
    const clampedY = Math.max(margin, Math.min(coords.y, mapHeight - margin));
    
    // Remove existing user marker if any
    const existing = mapContainer.querySelector('.user-marker');
    if (existing) existing.remove();
    
    const marker = document.createElement('div');
    marker.className = 'user-marker';
    marker.style.left = `${clampedX}px`;
    marker.style.top = `${clampedY}px`;
    marker.title = 'Your Location';
    
    // Optional: Add indicator if marker is clamped
    const isClamped = clampedX !== coords.x || clampedY !== coords.y;
    if (isClamped) {
        marker.title = 'Your Location (outside map area)';
        // You could add a visual indicator, like pulsing faster
        marker.style.animation = 'pulse 1s infinite'; // Faster pulse
    }
    
    mapContainer.appendChild(marker);
}

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

function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 3959; // Earth's radius in miles
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    
    return Math.round(distance * 100) / 100;
}

function toRad(degrees) {
    return degrees * (Math.PI / 180);
}

function getDirections(place) {
    if (!place) return;
    // Open Google Maps with directions
    const mapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${place.lat},${place.lng}&travelmode=walking`;
    window.open(mapsUrl, '_blank');
}

// Show all reviews
async function showReviewsModal() {
    if (!currentLocation) return;
    
    document.getElementById('reviewsModal').classList.add('active');
    document.body.style.overflow = 'hidden';
    document.getElementById('reviewsLocationName').textContent = currentLocation.name;
    document.getElementById('reviewsRating').textContent = currentLocation.rating.toFixed(1);
    document.getElementById('reviewsStars').innerHTML = generateStars(currentLocation.rating);
    
    // Setup sort listener
    const sortSelect = document.getElementById('reviewsSortSelect');
    if (sortSelect) {
        sortSelect.removeEventListener('change', handleSortChange);
        sortSelect.addEventListener('change', handleSortChange);
    }
    
    loadAllReviews(currentLocation.id, 1, 'recent');
}

async function handleSortChange(e) {
    if (currentLocation) {
        loadAllReviews(currentLocation.id, 1, e.target.value);
    }
}

async function loadAllReviews(locationId, page = 1, sortBy = 'recent') {
    try {
        const response = await fetch(
            `${CONFIG.API_BASE_URL}/reviews/${locationId}?page=${page}&pageSize=10&sortBy=${sortBy}`,
            { credentials: 'include' }
        );
        const result = await response.json();
        const reviews = result.success && result.data && result.data.reviews ? result.data.reviews : [];
        totalReviews = result.data?.totalReviews || 0;
        totalPages = result.data?.totalPages || 1;
        currentPage = page;
        
        // Update review count
        document.getElementById('reviewsCount').textContent = `${totalReviews} review${totalReviews !== 1 ? 's' : ''}`;
        
        displayAllReviews(reviews);
        updatePagination();
    } catch (error) {
        console.error('Error:', error);
        displayAllReviews([]);
    }
}

function displayAllReviews(reviews) {
    const container = document.getElementById('reviewsList');
    if (!container) return;
    
    if (reviews.length === 0) {
        container.innerHTML = '<p class="empty-message">No reviews yet</p>';
        return;
    }
    
    container.innerHTML = reviews.map(r => `
        <div class="review-card">
            <div class="review-header">
                <div>
                    <strong>${r.author || 'Anonymous'}</strong>
                    <small>${formatDate(r.createdAt)}</small>
                </div>
                <div>${generateStars(r.rating)}</div>
            </div>
            <h4>${r.title}</h4>
            <p>${r.body}</p>
        </div>
    `).join('');
}

function updatePagination() {
    const pagination = document.getElementById('reviewsPagination');
    if (!pagination) return;
    
    // Hide pagination if only 1 page
    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }
    
    pagination.style.display = 'flex';
    
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    if (prevBtn) {
        prevBtn.disabled = currentPage <= 1;
        prevBtn.removeEventListener('click', handlePrevPage);
        prevBtn.addEventListener('click', handlePrevPage);
    }
    
    if (nextBtn) {
        nextBtn.disabled = currentPage >= totalPages;
        nextBtn.removeEventListener('click', handleNextPage);
        nextBtn.addEventListener('click', handleNextPage);
    }
}

function handlePrevPage() {
    if (currentPage > 1 && currentLocation) {
        const sortBy = document.getElementById('reviewsSortSelect')?.value || 'recent';
        loadAllReviews(currentLocation.id, currentPage - 1, sortBy);
    }
}

function handleNextPage() {
    if (currentPage < totalPages && currentLocation) {
        const sortBy = document.getElementById('reviewsSortSelect')?.value || 'recent';
        loadAllReviews(currentLocation.id, currentPage + 1, sortBy);
    }
}

// In the write review success handler (wherever you handle the review submission response)
async function handleReviewSubmitSuccess(locationId) {
    // Reload the location to get updated rating
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/locations/${locationId}`, { 
            credentials: 'include' 
        });
        const data = await response.json();
        
        if (data.success && data.data) {
            // Update the location in allPlaces array
            const placeIndex = allPlaces.findIndex(p => p.id == locationId);
            if (placeIndex !== -1) {
                allPlaces[placeIndex].rating = data.data.rating || data.data.averageRating;
            }
            
            // Update current location if it's the same
            if (currentLocation && currentLocation.id == locationId) {
                currentLocation.rating = data.data.rating || data.data.averageRating;
            }
            
            // Refresh the display
            displayPlaces(allPlaces);
        }
    } catch (error) {
        console.error('Error updating rating:', error);
    }
}