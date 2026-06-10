// Common JavaScript for Suraksha Shield Frontend
// Handles JWT authentication, request headers, page layout rendering (navbar/footer), and flash notifications.

document.addEventListener('DOMContentLoaded', () => {
    renderNavbar();
    renderFooter();
});

// Authentication LocalStorage Helpers
function getToken() {
    return localStorage.getItem('jwt_token');
}

function getRole() {
    return localStorage.getItem('user_role'); // 'USER' or 'ADMIN'
}

function getUserInfo() {
    return {
        id: localStorage.getItem('user_id'),
        email: localStorage.getItem('user_email'),
        firstName: localStorage.getItem('user_firstName'),
        lastName: localStorage.getItem('user_lastName')
    };
}

function saveAuth(jwtResponse) {
    localStorage.setItem('jwt_token', jwtResponse.token);
    localStorage.setItem('user_id', jwtResponse.id);
    localStorage.setItem('user_email', jwtResponse.email);
    localStorage.setItem('user_role', jwtResponse.role);
    if (jwtResponse.firstName) {
        localStorage.setItem('user_firstName', jwtResponse.firstName);
    }
    if (jwtResponse.lastName) {
        localStorage.setItem('user_lastName', jwtResponse.lastName);
    }
}

function clearAuth() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_firstName');
    localStorage.removeItem('user_lastName');
}

function logout() {
    clearAuth();
    window.location.href = '/';
}

// Fetch Wrapper with JWT Header
async function fetchWithAuth(url, options = {}) {
    const token = getToken();
    const headers = options.headers || {};
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Set default Content-Type to application/json if sending body and not multipart
    if (options.body && !(options.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }
    
    options.headers = headers;
    
    const response = await fetch(url, options);
    
    if (response.status === 401 || response.status === 403) {
        // Token expired or invalid, redirect to login
        clearAuth();
        window.location.href = '/login';
    }
    
    return response;
}

// Render dynamic navbar replacing EJS header partial
function renderNavbar() {
    const placeholder = document.getElementById('navbar-placeholder');
    if (!placeholder) return;

    const token = getToken();
    const role = getRole();
    const user = getUserInfo();

    let navRightContent = '';
    
    if (token) {
        if (role === 'ADMIN') {
            navRightContent = `
                <a href="/admin/dashboard" class="btn btn-outline-danger fw-semibold px-4 rounded-3 shadow-sm me-2">Admin Dashboard</a>
                <button onclick="logout()" class="btn btn-danger fw-semibold px-4 rounded-3 shadow-sm">Log Out</button>
            `;
        } else {
            navRightContent = `
                <span class="text-secondary me-3 d-none d-md-inline">Welcome, <strong>${user.firstName || 'User'}</strong></span>
                <a href="/profile" class="btn btn-outline-primary fw-semibold px-4 rounded-3 shadow-sm me-2">My Profile</a>
                <button onclick="logout()" class="btn btn-outline-danger fw-semibold px-4 rounded-3 shadow-sm">Log Out</button>
            `;
        }
    } else {
        navRightContent = `
            <a href="/login" class="text-primary text-decoration-none fw-semibold me-3">Log In</a>
            <a href="/register" class="btn btn-primary fw-semibold px-4 rounded-3 shadow-sm">Register</a>
        `;
    }

    placeholder.innerHTML = `
        <nav class="navbar navbar-expand-lg sticky-top shadow-sm py-3 px-4">
            <div class="container">
                <a class="navbar-brand text-primary font-headline fw-bold" style="font-size: 1.5rem;" href="/">Suraksha Shield</a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0 fw-medium">
                        <li class="nav-item">
                            <a class="nav-link" href="/policies">Find a Policy</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="/#how-it-works">How it Works</a>
                        </li>
                    </ul>
                    <div class="d-flex align-items-center gap-3">
                        ${navRightContent}
                    </div>
                </div>
            </div>
        </nav>
        <div id="flash-placeholder" class="container mt-3"></div>
    `;
}

// Render dynamic footer replacing EJS footer partial
function renderFooter() {
    const placeholder = document.getElementById('footer-placeholder');
    if (!placeholder) return;

    placeholder.innerHTML = `
        <footer class="bg-light w-100 border-top mt-auto py-5">
            <div class="container px-4">
                <div class="row align-items-center">
                    <div class="col-md-6 text-center text-md-start mb-3 mb-md-0">
                        <span class="fs-5 fw-bold text-primary font-headline d-block mb-1">Suraksha Shield</span>
                        <span class="small text-secondary">© 2026 Suraksha Shield. Secure. Optimized.</span>
                    </div>
                    <div class="col-md-6 d-flex flex-wrap justify-content-center justify-content-md-end gap-4 small text-secondary">
                        <a class="text-secondary text-decoration-none" href="#">Terms of Service</a>
                        <a class="text-secondary text-decoration-none" href="#">Privacy Policy</a>
                        <a class="text-secondary text-decoration-none" href="#">Contact Us</a>
                    </div>
                </div>
            </div>
        </footer>
    `;
}

// Client-side Alert Message (flash message replacement)
function showFlash(message, type = 'success') {
    const flashPlaceholder = document.getElementById('flash-placeholder');
    if (!flashPlaceholder) return;

    flashPlaceholder.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}
