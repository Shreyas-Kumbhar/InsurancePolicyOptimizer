const express = require("express");
const app = express();
const path = require("path");
const mongoose = require("mongoose");
const session = require("express-session");
const flash = require('connect-flash');
const methodOverride = require('method-override');

const authRoutes = require('./routes/auth');
const policyRoutes = require('./routes/policies');
const userRoutes = require('./routes/user');
const adminRoutes = require('./routes/admin');

const MONGO_URL = "mongodb://127.0.0.1:27017/policiesDB";

//starting db
main()
  .then(() => {
    console.log("Database connected...");
  })
  .catch((err) => {
    console.log(err);
  });

//set up DB
async function main() {
  await mongoose.connect(MONGO_URL);
}

//views & static files
app.set("view engine", "ejs");
app.set("views", path.join(__dirname, "views"));
app.use(express.static(path.join(__dirname, "public")));
app.use(express.urlencoded({ extended: true }));
app.use(methodOverride('_method'));

app.use(session({
  secret: 'vaultsecretkey992!',
  resave: false,
  saveUninitialized: false
}));

app.use(flash());

app.use(async (req, res, next) => {
  res.locals.currentUser = req.session.userId;
  res.locals.success = req.flash('success');
  res.locals.error = req.flash('error');
  next();
});

//Routes
app.get("/", (req, res) => {
  res.render("landing.ejs");
});

app.use('/', authRoutes);
app.use('/', userRoutes);
app.use('/policies', policyRoutes);
app.use('/admin', adminRoutes);

app.listen(3647, () => {
  console.log("App listening on port 3647");
});