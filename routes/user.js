const express = require('express');
const router = express.Router();
const User = require('../models/user');
const { isLoggedIn } = require('../middleware/auth');

router.get('/profile', isLoggedIn, async (req, res) => {
    try {
        const user = await User.findById(req.session.userId).populate('allocatedPolicies');
        res.render('profile.ejs', { user });
    } catch (err) {
        console.error(err);
        req.flash('error', 'Could not load profile.');
        res.redirect('/policies');
    }
});

module.exports = router;
