const express = require('express');
const router = express.Router();
const User = require('../models/user');
const bcrypt = require('bcryptjs');

router.get('/login', (req, res) => {
    res.render('login.ejs');
});

router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;
        const user = await User.findOne({ email });
        if (!user) {
            req.flash('error', 'Invalid email or password. Please try again.');
            return res.redirect('/login');
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            req.flash('error', 'Invalid email or password. Please try again.');
            return res.redirect('/login');
        }

        req.session.userId = user._id;
        req.flash('success', 'Welcome back!');
        res.redirect('/policies');
    } catch (err) {
        console.error(err);
        req.flash('error', 'An error occurred during login.');
        res.redirect('/login');
    }
});

router.get('/register', (req, res) => {
    res.render('register.ejs');
});

router.post('/register', async (req, res) => {
    try {
        const { firstName, lastName, email, password } = req.body;
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            req.flash('error', 'User with this email already exists.');
            return res.redirect('/register');
        }

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        const newUser = new User({
            firstName,
            lastName,
            email,
            password: hashedPassword
        });

        await newUser.save();
        req.flash('success', 'Registration successful! Please login.');
        res.redirect('/login');
    } catch (err) {
        console.error(err);
        req.flash('error', 'Error registering user.');
        res.redirect('/register');
    }
});

router.get('/logout', (req, res) => {
    req.session.destroy(() => {
        res.redirect('/');
    });
});

module.exports = router;
