package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
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
import edu.brown.cs.h2r.baking.Recipes.CucumberSalad;
import edu.brown.cs.h2r.baking.Recipes.DeviledEggs;
import edu.brown.cs.h2r.baking.Recipes.MashedPotatoes;
import edu.brown.cs.h2r.baking.Recipes.MoltenLavaCake;
import edu.brown.cs.h2r.baking.Recipes.PeanutButterCookies;
import edu.brown.cs.h2r.baking.Recipes.PecanPie;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class SubgoalDetermination {

	public static Domain generateDomain(Recipe recipe) {
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
		
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain, recipe.topLevelIngredient);
		//Action hand = new HandAction(domain, recipe.topLevelIngredient);
		//Action waitAction = new WaitAction(domain);

		recipe.init(domain);
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	public static State generateInitialState(Domain domain, Recipe recipe) {
		State state = new State();
		
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish", "melting_pot");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_COUNTER, containers, "human"));

		state.addObject(ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_dish", null, SpaceFactory.SPACE_COUNTER));
		state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, SpaceFactory.SPACE_COUNTER));
		state.addObject(SpaceFactory.getNewBakingSpaceObjectInstance(domain, SpaceFactory.SPACE_OVEN, null, ""));
		state.addObject(SpaceFactory.getNewHeatingSpaceObjectInstance(domain, SpaceFactory.SPACE_STOVE, null, ""));
		
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = new Knowledgebase();
		List<ObjectInstance> ingredients = knowledgebase.getRecipeObjectInstanceList(state, domain, recipe);
		//knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
	
		for (ObjectInstance ingredientInstance : ingredients) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = state.getObject(SpaceFactory.SPACE_COUNTER);

		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredients, counterSpace.getName());
		
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, counterSpace.getName());
				state.addObject(containerInstance);
			}
		}

		for (ObjectInstance ingredientInstance : ingredients) {
			if (!IngredientFactory.isHiddenIngredient(ingredientInstance)) {
				if (IngredientFactory.getUseCount(ingredientInstance) >= 1) {
					ObjectInstance ing = state.getObject(ingredientInstance.getName());
					IngredientFactory.changeIngredientContainer(ing, ing.getName()+"_bowl");
					ContainerFactory.addIngredient(state.getObject(ing.getName()+"_bowl"), ing.getName());
					SpaceFactory.addContainer(state.getObject(SpaceFactory.SPACE_COUNTER), state.getObject(ing.getName()+"_bowl"));
				}
			}
		}
		return state;
	}
	
	public static List<KitchenSubdomain> generatePolicies(Recipe recipe) {
		// Add our actions to the domain.
		List<KitchenSubdomain> policyDomains = new ArrayList<KitchenSubdomain>();
		
		Domain domain = SubgoalDetermination.generateDomain(recipe);
		
		State state = SubgoalDetermination.generateInitialState(domain, recipe);
		AffordanceCreator theCreator = new AffordanceCreator(domain, state, recipe.topLevelIngredient);
		
		System.out.println("\n\nPlanner will now plan the "+recipe.topLevelIngredient.getName()+" recipe!");
		
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
				System.out.println("Planning subgoal " + sg.getIngredient().getName());
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
		List<PropositionalFunction> propFunctions = domain.getPropFunctions();
		for (PropositionalFunction pf : propFunctions) {
			((BakingPropositionalFunction)pf).changeTopLevelIngredient(ingredient);
			((BakingPropositionalFunction)pf).setSubgoal(subgoal);
		}
		
		subgoal.getGoal().changeTopLevelIngredient(ingredient);
		final PropositionalFunction isSuccess = subgoal.getGoal();
		
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
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
			planner = new BellmanAffordanceRTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
			planner.toggleDebugPrinting(false);
			planner.planFromState(currentState);
			
			// Create a Q-greedy policy from the planner
			p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);

		} else {
			planner = new RTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth);
			planner.planFromState(currentState);
			
			// Create a Q-greedy policy from the planner
			p = new GreedyQPolicy((QComputablePlanner)planner);
		}
		return KitchenSubdomain.makeSubdomain(domain, recipe, subgoal, startingState, p);
	}

	public static List<KitchenSubdomain> generateAllPolicies() {
		List<KitchenSubdomain> policyDomains = new ArrayList<KitchenSubdomain>();
		List<Recipe> recipes = Arrays.asList(
				new Brownies(), 
				new CucumberSalad(), 
				new DeviledEggs(), 
				new MashedPotatoes(),
				new MoltenLavaCake(),
				new PeanutButterCookies());
		
		for (Recipe recipe : recipes) {
			policyDomains.addAll(SubgoalDetermination.generatePolicies(recipe));
		}
		return policyDomains;
	}
	
	public static State generateRandomStateFromPolicy(KitchenSubdomain subdomain, int maxDepth) {
		Random rando = new Random();
		int randomDepth = rando.nextInt(maxDepth);
		Policy policy = subdomain.getPolicy();
		State state = subdomain.getStartState();
		for (int i = 0; i < randomDepth; i++) {
			AbstractGroundedAction action = policy.getAction(state);
			state = action.executeIn(state);
		}
		return state;
	}
	
	public static void insertPolicy(Map<String, Double> bestPolicies, PolicyProbability probability, KitchenSubdomain subdomain) {
		String key = subdomain.getRecipe().topLevelIngredient.getName() + " - " + subdomain.getSubgoal().getIngredient().getName();
		Double value = probability.getProbability();
		if (bestPolicies.size() < 5) {
			bestPolicies.put(key, value);
		} else {
			boolean insert = false;
			Map.Entry<String, Double> minEntry = null;
			for (Map.Entry<String, Double> entry : bestPolicies.entrySet()) {
				if (minEntry != null && entry.getValue() > minEntry.getValue()) {
					minEntry = entry;
				}
				insert |= entry.getValue() < value;
			}
			if (insert) {
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
			SubgoalDetermination.insertPolicy(bestPolicies, probability, subdomain);
		}
		
		return bestPolicies;
		
	}
	
	public static void main(String[] argv) {
		List<KitchenSubdomain> policyDomains = SubgoalDetermination.generateAllPolicies();
		
		PolicyPrediction prediction = new PolicyPrediction(policyDomains);
		Random rando = new Random();
		for (int i = 0; i < 2; i++) {
			KitchenSubdomain policyDomain = policyDomains.get(rando.nextInt(policyDomains.size()));
			State state = SubgoalDetermination.generateRandomStateFromPolicy(policyDomain, 5);
			System.out.println("Actual: "  + policyDomain.getRecipe().topLevelIngredient.getName() + " - " + policyDomain.getSubgoal().getIngredient().getName());

			List<PolicyProbability> policyDistribution = 
					prediction.getPolicyDistributionFromStatePair(policyDomain.getStartState(), state, 5);
		
			Map<String, Double> topPolicies = SubgoalDetermination.findTopPolicies(policyDistribution, policyDomains, 10);
			for (Map.Entry<String, Double> entry : topPolicies.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
			
		}
			
	}
}
