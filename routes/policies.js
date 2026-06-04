const express = require('express');
const router = express.Router();
const Policy = require('../models/policy');
const User = require('../models/user');
const { isLoggedIn } = require('../middleware/auth');

router.get('/', (req, res) => {
  res.render("policies/config.ejs");
});

router.get('/results', isLoggedIn, async (req, res) => {
  if (req.query.show_all === "true") {
    const policies = await Policy.find({}).sort({ premium: 1 });
    return res.render("policies/results.ejs", { policies, combinationMode: false });
  }

  let { max_premium, coverage_min, coverage_max, type, riskLevel, name } = req.query;

  // Default values if no search query provided
  max_premium = max_premium ? parseInt(max_premium) : 20000;
  coverage_min = coverage_min ? parseInt(coverage_min) : 0;
  coverage_max = coverage_max ? parseInt(coverage_max) : 5000000;

  const filter = {};
  if (type && type !== "All Types") filter.type = type.toLowerCase();
  if (riskLevel && riskLevel !== "All Levels") filter.riskLevel = riskLevel.toLowerCase();

  // If user searched by name, skip backtracking and just filter
  if (name) {
    filter.name = { $regex: name, $options: "i" };
    const policies = await Policy.find(filter).sort({ premium: 1 });
    return res.render("policies/results.ejs", { policies, combinationMode: false });
  }

  // Backtracking Algorithm for Optimization Engine
  const allPolicies = await Policy.find(filter);

  let bestCombination = [];
  let minPremium = Infinity;

  function findOptimalPolicies(index, currentCombination, currentPremium, currentCoverage) {
    // If we meet coverage requirement and stay within premium limit, check if it's the best
    if (currentCoverage >= coverage_min && currentPremium <= max_premium) {
      if (currentPremium < minPremium) {
        minPremium = currentPremium;
        bestCombination = [...currentCombination];
      }
    }

    // Base cases to stop recursion
    if (index >= allPolicies.length || currentPremium > max_premium) {
      return;
    }

    // Choice 1: Exclude the current policy
    findOptimalPolicies(index + 1, currentCombination, currentPremium, currentCoverage);

    // Choice 2: Include the current policy
    const policy = allPolicies[index];
    if (currentPremium + policy.premium <= max_premium && currentCoverage + policy.coverage <= coverage_max) {
      currentCombination.push(policy);
      findOptimalPolicies(index + 1, currentCombination, currentPremium + policy.premium, currentCoverage + policy.coverage);
      currentCombination.pop(); // backtrack
    }
  }

  findOptimalPolicies(0, [], 0, 0);

  bestCombination.sort((a, b) => a.premium - b.premium);

  const totalCoverage = bestCombination.reduce((sum, p) => sum + p.coverage, 0);

  res.render("policies/results.ejs", {
    policies: bestCombination,
    combinationMode: true,
    totalPremium: minPremium === Infinity ? 0 : minPremium,
    totalCoverage: totalCoverage
  });
});

router.post('/:id/allocate', isLoggedIn, async (req, res) => {
    try {
        const user = await User.findById(req.session.userId);
        const policyId = req.params.id;
        
        // Prevent duplicate allocation
        if (!user.allocatedPolicies.includes(policyId)) {
            user.allocatedPolicies.push(policyId);
            await user.save();
            req.flash('success', 'Policy successfully allocated to your profile!');
        } else {
            req.flash('error', 'You have already allocated this policy.');
        }
        res.redirect('/profile');
    } catch (err) {
        console.error(err);
        req.flash('error', 'Failed to allocate policy.');
        res.redirect(`/policies/${req.params.id}`);
    }
});

router.get('/:id', async (req, res) => {
  const policy = await Policy.findById(req.params.id);
  res.render("policies/detail.ejs", { policy });
});

module.exports = router;
