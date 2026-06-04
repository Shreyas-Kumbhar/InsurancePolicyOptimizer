const express = require('express');
const router = express.Router();
const Admin = require('../models/admin');
const Policy = require('../models/policy');
const bcrypt = require('bcryptjs');

// Middleware to check if admin is logged in
const isAdminLoggedIn = (req, res, next) => {
    if (!req.session.adminId) {
        req.flash('error', 'You must be logged in as an admin to access that area.');
        return res.redirect('/admin/login');
    }
    next();
};

// Pass admin session info to locals for admin views
router.use((req, res, next) => {
    res.locals.adminId = req.session.adminId;
    next();
});

// Admin Login GET
router.get('/login', (req, res) => {
    res.render('admin/login.ejs');
});

// Admin Login POST
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;
        const admin = await Admin.findOne({ email });
        if (!admin) {
            req.flash('error', 'Invalid admin credentials.');
            return res.redirect('/admin/login');
        }

        const isMatch = await bcrypt.compare(password, admin.password);
        if (!isMatch) {
            req.flash('error', 'Invalid admin credentials.');
            return res.redirect('/admin/login');
        }

        req.session.adminId = admin._id;
        req.flash('success', 'Admin login successful!');
        res.redirect('/admin/dashboard');
    } catch (e) {
        console.error(e);
        req.flash('error', 'An error occurred during admin login.');
        res.redirect('/admin/login');
    }
});

// Admin Logout
router.get('/logout', (req, res) => {
    req.session.destroy(() => {
        res.redirect('/admin/login');
    });
});

// Admin Dashboard - List Policies
router.get('/dashboard', isAdminLoggedIn, async (req, res) => {
    try {
        const policies = await Policy.find({});
        res.render('admin/dashboard.ejs', { policies });
    } catch (e) {
        console.error(e);
        req.flash('error', 'Failed to load policies.');
        res.redirect('/');
    }
});

// Admin - New Policy Form
router.get('/policies/new', isAdminLoggedIn, (req, res) => {
    res.render('admin/newPolicy.ejs');
});

// Admin - Create Policy
router.post('/policies', isAdminLoggedIn, async (req, res) => {
    try {
        const newPolicy = new Policy(req.body.policy);

        // Store admin id
        newPolicy.createdBy = req.session.adminId;
        console.log(req.session.adminId);

        await newPolicy.save();

        req.flash('success', 'Policy added successfully!');
        res.redirect('/admin/dashboard');
    } catch (e) {
        console.error(e);
        req.flash('error', 'Failed to add policy.');
        res.redirect('/admin/policies/new');
    }
});

// Admin - Edit Policy Form
router.get('/policies/:id/edit', isAdminLoggedIn, async (req, res) => {
    try {
        const policy = await Policy.findById(req.params.id);
        if (!policy) {
            req.flash('error', 'Policy not found.');
            return res.redirect('/admin/dashboard');
        }
        res.render('admin/editPolicy.ejs', { policy });
    } catch (e) {
        console.error(e);
        req.flash('error', 'Failed to find policy.');
        res.redirect('/admin/dashboard');
    }
});

// Admin - Update Policy
router.put('/policies/:id', isAdminLoggedIn, async (req, res) => {
    try {
        await Policy.findByIdAndUpdate(req.params.id, req.body.policy, { runValidators: true });
        req.flash('success', 'Policy updated successfully!');
        res.redirect('/admin/dashboard');
    } catch (e) {
        console.error(e);
        req.flash('error', 'Failed to update policy.');
        res.redirect(`/admin/policies/${req.params.id}/edit`);
    }
});

// Admin - Delete Policy
router.delete('/policies/:id', isAdminLoggedIn, async (req, res) => {
    try {
        await Policy.findByIdAndDelete(req.params.id);
        req.flash('success', 'Policy deleted successfully!');
        res.redirect('/admin/dashboard');
    } catch (e) {
        console.error(e);
        req.flash('error', 'Failed to delete policy.');
        res.redirect('/admin/dashboard');
    }
});

module.exports = router;
