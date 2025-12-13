// USC Food Locator - SIMPLIFIED VERSION
// Only essential functionality, no dead code

const CONFIG = {
    USC_CENTER: [34.0224, -118.2851],
    MAP_BOUNDS: {
        north: 34.0280,
        south: 34.0200,
        east: -118.2820,
        west: -118.2880
    },
    API_BASE_URL: '/foodlocator/api'
};

let mapContainer;
let currentLocation = null;
let allPlaces = [];
let locationMarkers = [];
let currentReviews = [];
let currentPage = 1;
let totalReviews = 0;
let totalPages = 1;

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
    }
}

function locateMe() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                const userLat = latitude;
                const userLng = longitude;
                
                // Find nearest location
                if (allPlaces.length > 0) {
                    let nearest = allPlaces[0];
                    let minDist = Math.sqrt(Math.pow(nearest.lat - userLat, 2) + Math.pow(nearest.lng - userLng, 2));
                    
                    allPlaces.forEach(place => {
                        const dist = Math.sqrt(Math.pow(place.lat - userLat, 2) + Math.pow(place.lng - userLng, 2));
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = place;
                        }
                    });
                    
                    showLocationModal(nearest);
                }
            },
            (error) => {
                console.error('Geolocation error:', error);
                showError('Could not get your location');
            }
        );
    } else {
        showError('Geolocation not supported');
    }
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
