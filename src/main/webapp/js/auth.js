// ============================================================================
// Authentication Module
// ============================================================================

let currentUser = null; // Global variable to track current logged-in user

/**
 * Display the login/register modal
 */
function showLoginModal() {
    const modal = document.getElementById('loginModal');
    showLoginForm();
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

/**
 * Switch to login form view in the modal
 */
function showLoginForm() {
    document.getElementById('loginContainer').style.display = 'block';
    document.getElementById('registerContainer').style.display = 'none';
    document.getElementById('loginForm').reset();
}

/**
 * Switch to register form view in the modal
 */
function showRegisterForm() {
    document.getElementById('loginContainer').style.display = 'none';
    document.getElementById('registerContainer').style.display = 'block';
    document.getElementById('registerForm').reset();
}

/**
 * Handle login form submission
 * Sends credentials to backend and handles response
 */
async function handleLogin(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const username = formData.get('username');
    const password = formData.get('password');
    
    // Validate inputs
    if (!username || !password) {
        alert('Please fill in all fields');
        return;
    }
    
    try {
        showLoading(true);
        
        // Create form data for submission
        const loginFormData = new FormData();
        loginFormData.append('username', username);
        loginFormData.append('password', password);
        
        // Call backend login servlet
        const response = await fetch('/login', {
            method: 'POST',
            body: loginFormData,
            credentials: 'same-origin' // Include cookies for session
        });
        
        showLoading(false);
        
        // Parse JSON response from LoginServlet
        const data = await response.json();
        
        if (data.success) {
            // Login was successful
            currentUser = { username: data.username, userId: data.userId };
            sessionStorage.setItem('user', JSON.stringify(currentUser));
            
            closeModal('loginModal');
            updateLoginButton(currentUser);
            alert('Login successful!');
        } else {
            alert(data.error || 'Login failed. Invalid username or password.');
        }
    } catch (error) {
        console.error('Error logging in:', error);
        showLoading(false);
        alert('Login failed. Please try again.');
    }
}

/**
 * Handle signup form submission
 * Creates new user account and logs them in
 */
async function handleRegister(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const email = formData.get('email');
    const username = formData.get('username');
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    
    // Validate inputs
    if (!email || !username || !password || !confirmPassword) {
        alert('Please fill in all fields');
        return;
    }
    
    // Validate passwords match
    if (password !== confirmPassword) {
        alert('Passwords do not match!');
        return;
    }
    
    // Validate password length
    if (password.length < 6) {
        alert('Password must be at least 6 characters long!');
        return;
    }
    
    try {
        showLoading(true);
        
        // Create form data for submission
        const signupFormData = new FormData();
        signupFormData.append('email', email);
        signupFormData.append('username', username);
        signupFormData.append('password', password);
        signupFormData.append('confirm', confirmPassword);
        
        // Call backend signup servlet
        const response = await fetch('/signup', {
            method: 'POST',
            body: signupFormData
        });
        
        showLoading(false);
        
        // Parse JSON response from SignupServlet
        const data = await response.json();
        
        if (data.success) {
            // Store user info in sessionStorage
            currentUser = { username: data.username, userId: data.userId };
            sessionStorage.setItem('user', JSON.stringify(currentUser));
            
            closeModal('loginModal');
            updateLoginButton(currentUser);
            alert('Signup successful! You are now logged in.');
        } else {
            alert(data.error || 'Signup failed. Please try again.');
        }
    } catch (error) {
        console.error('Error registering:', error);
        showLoading(false);
        alert('Signup failed. Please try again.');
    }
}

/**
 * Update the login button appearance based on login status
 * @param {Object} user - Current user object or null if not logged in
 */
function updateLoginButton(user) {
    const loginBtn = document.getElementById('loginBtn');
    
    if (user) {
        loginBtn.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
            </svg>
            ${user.username || user.name || 'User'}
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

/**
 * Log out the current user
 */
function logout() {
    currentUser = null; // Clear the global currentUser variable
    sessionStorage.removeItem('user');
    updateLoginButton(null);
    alert('Logged out successfully!');
}

/**
 * Check if user is logged in and restore session from sessionStorage
 * Called on page load to maintain login state
 */
function checkLoginStatus() {
    const userStr = sessionStorage.getItem('user');
    if (userStr) {
        const user = JSON.parse(userStr);
        currentUser = user; // Set the global currentUser variable
        updateLoginButton(user);
    } else {
        currentUser = null;
    }
}
