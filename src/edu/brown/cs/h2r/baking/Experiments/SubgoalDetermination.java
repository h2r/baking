package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Prediction.PolicyPrediction;
import Prediction.PolicyProbability;
import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.BellmanAffordanceRTDP;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.PeanutButterCookies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class SubgoalDetermination {
	private final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	public static Domain generateGeneralDomain(List<Recipe> recipes) {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(ToolFactory.createObjectClass(domain));
		domain.setObjectIdentiferDependence(true);
		
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	public static Domain generateSpecificDomain(Domain generalDomain, Recipe recipe) {
		Domain domain = new SADomain();
		for (ObjectClass objectClass : generalDomain.getObjectClasses()) {
			domain.addObjectClass(objectClass);
		}
		
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain, recipe.topLevelIngredient);
		//Action hand = new HandAction(domain, recipe.topLevelIngredient);
		//Action waitAction = new WaitAction(domain);

		recipe.init(domain);
		return domain;
	}
	
	public static State generateInitialState(Domain generalDomain, List<Domain> recipeDomains, List<Recipe> recipes) {
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		objects.add(AgentFactory.getNewHumanAgentObjectInstance(generalDomain, "human"));
		
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish", "melting_pot");
		ObjectInstance counterSpace = SpaceFactory.getNewWorkingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_COUNTER, containers, "human");
		objects.add(counterSpace);
		
		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(generalDomain, "baking_dish", null, SpaceFactory.SPACE_COUNTER));
		objects.add(ContainerFactory.getNewHeatingContainerObjectInstance(generalDomain, "melting_pot", null, SpaceFactory.SPACE_COUNTER));
		objects.add(SpaceFactory.getNewBakingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_OVEN, null, ""));
		objects.add(SpaceFactory.getNewHeatingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_STOVE, null, ""));
		
		for (String container : containers) { 
			objects.add(ContainerFactory.getNewMixingContainerObjectInstance(generalDomain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = new Knowledgebase();
		
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		List<ObjectInstance> tools = new ArrayList<ObjectInstance>();
		for (int i = 0;i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			Domain domain = recipeDomains.get(i);
			ingredients.addAll(knowledgebase.getRecipeObjectInstanceList(domain, recipe));
			tools.addAll(knowledgebase.getTools(domain, SpaceFactory.SPACE_COUNTER));
		}
		
	
		ObjectClass containerClass = generalDomain.getObjectClass(ContainerFactory.ClassName);		
		
		List<ObjectInstance> containersAndIngredients = Recipe.getContainersAndIngredients(containerClass, ingredients, counterSpace.getName());
		objects.addAll(containersAndIngredients);
		objects.addAll(tools);
		return new State(objects);
	}
	
	public static List<KitchenSubdomain> generatePolicies(Recipe recipe, Domain domain, State state) {
		// Add our actions to the domain.
		
		List<KitchenSubdomain> policyDomains = new ArrayList<KitchenSubdomain>();
		
		AffordanceCreator theCreator = new AffordanceCreator(domain, state, recipe.topLevelIngredient);
		
		//System.out.println("\n\nPlanner will now plan the "+recipe.topLevelIngredient.getName()+" recipe!");
		
		// High level planner that plans through the recipe's subgoals
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		Set<BakingSubgoal> activeSubgoals = new HashSet<BakingSubgoal>();
		
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		if (((RecipeBotched)isFailure).hasNoSubgoals()) {
			for (BakingSubgoal sg : recipe.getIngredientSubgoals()) {
				((RecipeBotched)isFailure).addSubgoal(sg);
			}
		}
		
		do {
			// For all subgoals with all preconditions satisfied
			for (BakingSubgoal sg : activeSubgoals) {
				subgoals.remove(sg);
				//System.out.println("Planning subgoal " + sg.getIngredient().getName());
				KitchenSubdomain subdomain = generatePolicy(domain, state, recipe, sg);
				state = SubgoalDetermination.getEndState(domain, state, subdomain.getPolicy(), sg.getGoal(), isFailure);
				policyDomains.add(subdomain);
			}
			activeSubgoals.clear();
			// Iterate through inactive subgoals to find those who have had all of their
			// preconditions resolved.
			for (BakingSubgoal sg : subgoals) {
				if (sg.allPreconditionsCompleted(state)) {
					activeSubgoals.add(sg);
				}
			}	
		} while (!activeSubgoals.isEmpty());
		return policyDomains;
	}
	
	public static State getEndState(Domain domain, State state, Policy policy, BakingPropositionalFunction isSuccess, PropositionalFunction isFailure) {
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		RewardFunction rf = new RecipeAgentSpecificMakeSpanRewardFunction("human");
		EpisodeAnalysis episodeAnalysis = policy.evaluateBehavior(state, rf, recipeTerminalFunction,100);
		
		
		for (GroundedAction action : episodeAnalysis.actionSequence) {
			//System.out.println("\t" + action.actionName() + " " + Arrays.toString(action.params) );
		}
		
		return episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
		
	}
	
	public static KitchenSubdomain generatePolicy(Domain domain, State startingState, Recipe recipe, BakingSubgoal subgoal)
	{
		IngredientRecipe ingredient = subgoal.getIngredient();
		//String goalType = (subgoal.getGoal().getClass().isAssignableFrom(ContainersCleaned.class)) ? "_clean" : "";
		//System.out.println(ingredient.getName() + goalType);
		State currentState = new State(startingState);
		
		List<Action> actions = domain.getActions();
		for (Action action : actions) {
			((BakingAction)action).changePlanningIngredient(ingredient);
		}
		AffordanceCreator theCreator = new AffordanceCreator(domain, currentState, ingredient);
		// Add the current top level ingredient so we can properly trim the action space
		SubgoalDetermination.setSubgoal(domain, subgoal, ingredient);
		final PropositionalFunction isSuccess = subgoal.getGoal();
		
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		RewardFunction rf = new RecipeAgentSpecificMakeSpanRewardFunction("human");
		/*
				new RewardFunction() {
			@Override
			// Uniform cost function for an optimistic algorithm that guarantees convergence.
			public double reward(State state, GroundedAction a, State sprime) {
				return -1;
			}
		};*/
		
		int numRollouts = 300; // RTDP
		int maxDepth = 10; // RTDP
		double vInit = 0;
		double maxDelta = .01;
		double gamma = 0.99;
		
		boolean affordanceMode = true;
		RTDP planner;
		Policy p;
		AffordancesController affController = theCreator.getAffController();
		if(affordanceMode) {
			// RTDP planner that also uses affordances to trim action space during the Bellman update
			planner = new BellmanAffordanceRTDP(domain, rf, recipeTerminalFunction, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
			planner.toggleDebugPrinting(false);
			planner.planFromState(currentState);
			
			// Create a Q-greedy policy from the planner
			p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);

		} else {
			planner = new RTDP(domain, rf, recipeTerminalFunction, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);
			p = new GreedyQPolicy((QComputablePlanner)planner);
		}
		return KitchenSubdomain.makeSubdomain(domain, recipe, subgoal, startingState, p);
	}

	public static void setSubgoal(KitchenSubdomain subdomain) {
		Domain domain = subdomain.getDomain();
		BakingSubgoal subgoal = subdomain.getSubgoal();
		IngredientRecipe ingredient = subgoal.getIngredient();
		
		List<PropositionalFunction> propFunctions = domain.getPropFunctions();
		for (PropositionalFunction pf : propFunctions) {
			((BakingPropositionalFunction)pf).changeTopLevelIngredient(ingredient);
			((BakingPropositionalFunction)pf).setSubgoal(subgoal);
		}
		
		subgoal.getGoal().changeTopLevelIngredient(ingredient);
	}
	
	public static void setSubgoal(Domain domain, BakingSubgoal subgoal, IngredientRecipe ingredient) {
		List<PropositionalFunction> propFunctions = domain.getPropFunctions();
		for (PropositionalFunction pf : propFunctions) {
			((BakingPropositionalFunction)pf).changeTopLevelIngredient(ingredient);
			((BakingPropositionalFunction)pf).setSubgoal(subgoal);
		}
		
		subgoal.getGoal().changeTopLevelIngredient(ingredient);
	}

	public static List<KitchenSubdomain> generateAllPolicies() {
		List<KitchenSubdomain> policyDomains = new ArrayList<KitchenSubdomain>();
		List<Recipe> recipes = Arrays.asList(
				new Brownies(), 
				/*new CucumberSalad()/*, 
				new DeviledEggs(), 
				new MashedPotatoes(),
				new MoltenLavaCake(),
				*/new PeanutButterCookies());
		
		Domain generalDomain = SubgoalDetermination.generateGeneralDomain(recipes);
		List<Domain> recipeDomains = new ArrayList<Domain>();
		for (Recipe recipe : recipes) {
			recipeDomains.add(SubgoalDetermination.generateSpecificDomain(generalDomain, recipe));
		}
		State state = SubgoalDetermination.generateInitialState(generalDomain, recipeDomains, recipes);
		
		for (int i = 0; i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			Domain domain = recipeDomains.get(i);
			policyDomains.addAll(SubgoalDetermination.generatePolicies(recipe, domain, state));
		}
		
		return policyDomains;
	}
	
	public static State generateRandomStateFromPolicy(KitchenSubdomain subdomain, int maxDepth) {
		Policy policy = subdomain.getPolicy();
		State state = subdomain.getStartState();
		BakingSubgoal subgoal = subdomain.getSubgoal();
		
		//System.out.println("Taking actions");
		SubgoalDetermination.setSubgoal(subdomain);
		for (int i = 0; i < maxDepth; i++) {
			if (subgoal.goalCompleted(state)) {
				return null;
			}
			AbstractGroundedAction action = policy.getAction(state);
			//System.out.println("\t" + action.actionName() + " " + Arrays.toString(action.params) );
			state = action.executeIn(state);
		}
		return state;
	}
	
	public static void insertPolicy(Map<String, Double> bestPolicies, PolicyProbability probability, KitchenSubdomain subdomain, int numberToReturn) {
		String key = subdomain.getRecipe().topLevelIngredient.getName() + " - " + subdomain.getSubgoal().getIngredient().getName();
		Double value = probability.getProbability();
		if (bestPolicies.size() < numberToReturn) {
			bestPolicies.put(key, value);
		} else {
			boolean insert = false;
			Map.Entry<String, Double> minEntry = 
					bestPolicies.entrySet().iterator().next();
			
			for (Map.Entry<String, Double> entry : bestPolicies.entrySet()) {
				if (entry.getValue() < minEntry.getValue()) {
					minEntry = entry;
				}
				
			}
			if (minEntry.getValue() < value) {
				bestPolicies.remove(minEntry.getKey());
				bestPolicies.put(key, value);
			}
		}
	}
	
	public static Map<String, Double> findTopPolicies(List<PolicyProbability> distribution, List<KitchenSubdomain> subdomains, int numberToReturn) {
		Map<String, Double> bestPolicies = new HashMap<String, Double>();
		
		for (int i = 0; i < distribution.size(); i++) {
			PolicyProbability probability = distribution.get(i);
			KitchenSubdomain subdomain = subdomains.get(i);
			SubgoalDetermination.insertPolicy(bestPolicies, probability, subdomain, numberToReturn);
		}
		
		return bestPolicies;
		
	}
	
	public static String buildName(KitchenSubdomain policyDomain) {
		return policyDomain.getRecipe().topLevelIngredient.getName() + " - " + policyDomain.getSubgoal().getIngredient().getName();
	}
	
	public static void main(String[] argv) {
		int maxAlpha = 3;
		int numTries = 100;
		int depthType = 0;
		int depth = 0;
		
		if (argv.length == 1) {
			int Id = Integer.parseInt(argv[0]);
			int experimentModel = Id % 12;
			depthType = experimentModel % 3;
			depth = experimentModel / 4 + 1;
		}
		
		List<KitchenSubdomain> policyDomains = SubgoalDetermination.generateAllPolicies();
		List<KitchenSubdomain> testDomains = new ArrayList<KitchenSubdomain>(policyDomains);
		Random rando = new Random();
		
		
		List<Double> successRate = new ArrayList<Double>();
		PolicyPrediction prediction = new PolicyPrediction(policyDomains, depthType);			
		int numSuccess = 0;
		int numEstimateSuccesses = 0;
		int numRandomGuesses = 0;
		for (int i = 0; i < numTries; i++) {
			int randomIndex = rando.nextInt(testDomains.size());
			KitchenSubdomain policyDomain = testDomains.get(randomIndex);
			State state = SubgoalDetermination.generateRandomStateFromPolicy(policyDomain, depth);
			
			while (state == null) {
				testDomains.remove(randomIndex);
				randomIndex = rando.nextInt(testDomains.size());
				policyDomain = testDomains.get(randomIndex);
				state = SubgoalDetermination.generateRandomStateFromPolicy(policyDomain, depth);
			}
			String actualName = SubgoalDetermination.buildName(policyDomain);
			//System.out.println("Actual: " + actualName);
			List<PolicyProbability> policyDistribution = 
					prediction.getPolicyDistributionFromStatePair(policyDomain.getStartState(), state, maxAlpha+1, policyDomain, SubgoalDetermination.hashingFactory, depthType);
			
			if (policyDistribution == null) {
				continue;
			}
			double maxProb = 0.0;
			List<String> bestPolicies = new ArrayList<String>();
			for (int j = 0; j < policyDistribution.size(); j++) {
				String name = policyDomains.get(j).getRecipe().topLevelIngredient.getName() + " - " + policyDomains.get(j).getSubgoal().getIngredient().getName();
				PolicyProbability policyProbability = policyDistribution.get(j);
				
				
				double prob = (policyProbability == null) ? 0.0 : policyProbability.getProbability();
				if (prob > maxProb) {
					bestPolicies.clear();
					bestPolicies.add(name);
					maxProb = prob;	
				} else if (prob == maxProb) {
					bestPolicies.add(name);
				}
				//System.out.println(name + ": " + prob);
			}
			Collections.shuffle(bestPolicies, rando);
			if (bestPolicies.size() > 1) {
				numRandomGuesses++;
			}
			String choice = bestPolicies.get(0);
			//System.out.println("Best choice: " + choice);
			if (choice.equals(actualName)) {
				numSuccess++;
				if (bestPolicies.size() == 1) {
					numEstimateSuccesses++;
				}
			}
		}
		
		System.out.println("Depth, Depth Type, Successes, Estimate Successes, Informed Guesses, Total Trials");
		System.out.println("" + depth + depthType + numSuccess + ", " + numEstimateSuccesses + ", " + numRandomGuesses + ", " +  numTries);
		
		/*successRate.add((double)numSuccess / numTries);
		System.out.println("Success: " + numSuccess + "/" + numTries);
		System.out.println("Correct non guesses: " + numEstimateSuccesses + "/" + (numTries - numRandomGuesses));
		System.out.println("Educated Guesses: " + numRandomGuesses + "/" + numTries);
		
		
		for (int i = 0; i < successRate.size(); i++) {
			int depthType = i % 3;
			if (depthType == 0) {
				System.out.println("Depth type: " +  i / 3);
			}
			System.out.println(Integer.toString(i+1) + ": " + successRate.get(i));
		}*/
			
	}
}
