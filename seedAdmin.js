const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const Admin = require('./models/admin');

const MONGO_URL = "mongodb://127.0.0.1:27017/policiesDB";

mongoose.connect(MONGO_URL)
    .then(() => console.log('Database connected for seeding'))
    .catch(err => console.log('DB connection error:', err));

const seedAdmin = async () => {
    try {
        await Admin.deleteMany({}); // clear existing admins if any
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash('admin123', salt);
        
        const admin = new Admin({
            email: 'admin@gmail.com',
            password: hashedPassword
        });
        
        await admin.save();
        console.log('Admin user seeded successfully:');
        console.log('Email: admin@gmail.com');
        console.log('Password: admin123');
    } catch (e) {
        console.error('Error seeding admin:', e);
    } finally {
        mongoose.connection.close();
    }
};

seedAdmin();
