const mongoose = require("mongoose");
const initData = require("./data.js");
const Policy=require("../models/policy.js");

//connection establishment
const MONGO_URL="mongodb://127.0.0.1:27017/policiesDB";

async function main() { //for set up
    await mongoose.connect(MONGO_URL);
};

main()      //for start DB
    .then(()=>{
        console.log("Database connected..");
        // Move initDB here to run after connection
        initDB();
    })
    .catch((err)=>{
        console.log(err);
    });

//create a async function :Initialization of db
const initDB=async ()=>{
    await Policy.deleteMany({});
    await Policy.insertMany(initData.data);
    console.log("Data initialised");
}