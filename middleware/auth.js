const User = require('../models/user');

module.exports.isLoggedIn = (req, res, next) => {
    if (!req.session.userId) {
        req.flash('error', 'You must be signed in first!');
        return res.redirect('/login');
    }
    next();
};

module.exports.isAdmin = async (req, res, next) => {
    if (!req.session.userId) {
        req.flash('error', 'You must be signed in first!');
        return res.redirect('/login');
    }
    try {
        const user = await User.findById(req.session.userId);
        if (user && user.isAdmin) {
            next();
        } else {
            req.flash('error', 'You do not have permission to do that!');
            return res.redirect('/policies');
        }
    } catch (e) {
        req.flash('error', 'Something went wrong.');
        res.redirect('/policies');
    }
};
