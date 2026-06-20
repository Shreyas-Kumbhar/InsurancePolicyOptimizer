// Authentication management utilities

const auth = {
    getToken: () => sessionStorage.getItem('jwt_token'),
    getRole: () => sessionStorage.getItem('user_role'),
    getEmail: () => sessionStorage.getItem('user_email'),
    getName: () => sessionStorage.getItem('user_name'),
    
    setSession: (token, role, email, firstName, lastName) => {
        sessionStorage.setItem('jwt_token', token);
        sessionStorage.setItem('user_role', role);
        sessionStorage.setItem('user_email', email);
        if (firstName && lastName) {
            sessionStorage.setItem('user_name', `${firstName} ${lastName}`);
        } else {
            sessionStorage.removeItem('user_name');
        }
    },
    
    clearSession: () => {
        sessionStorage.removeItem('jwt_token');
        sessionStorage.removeItem('user_role');
        sessionStorage.removeItem('user_email');
        sessionStorage.removeItem('user_name');
    },
    
    isAuthenticated: () => {
        return !!auth.getToken();
    },
    
    isAdmin: () => {
        return auth.isAuthenticated() && auth.getRole() === 'ADMIN';
    },

    // Authenticated API fetch helper
    fetch: async (url, options = {}) => {
        const token = auth.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(url, {
            ...options,
            headers
        });
        
        if (response.status === 401 || response.status === 403) {
            // Token expired or invalid
            const currentPath = window.location.pathname;
            if (!currentPath.includes('login') && !currentPath.includes('register')) {
                auth.clearSession();
                if (currentPath.startsWith('/admin')) {
                    window.location.href = '/admin/login';
                } else {
                    window.location.href = '/login';
                }
            }
        }
        
        return response;
    },

    checkUserAuth: () => {
        if (!auth.isAuthenticated()) {
            window.location.href = '/login';
        }
    },

    checkAdminAuth: () => {
        if (!auth.isAdmin()) {
            window.location.href = '/admin/login';
        }
    }
};
