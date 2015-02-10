package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.parallel.Parallel;
import burlap.parallel.Parallel.ForEachCallable;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.BellmanAffordanceRTDP;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Experiments.ExperimentHelper;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.CerealWithMilk;
import edu.brown.cs.h2r.baking.Recipes.ChocolateChipCookies;
import edu.brown.cs.h2r.baking.Recipes.ChocolateMilk;
import edu.brown.cs.h2r.baking.Recipes.CoffeeWithMilk;
import edu.brown.cs.h2r.baking.Recipes.CranberryWalnutCookies;
import edu.brown.cs.h2r.baking.Recipes.FriedEgg;
import edu.brown.cs.h2r.baking.Recipes.Pancake;
import edu.brown.cs.h2r.baking.Recipes.PeanutButterCookies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Recipes.ScrambledEgg;
import edu.brown.cs.h2r.baking.Recipes.Tea;

public class AgentHelper {
	public static final Map<String, Map<GroundedAction, Double>> actionTimes = new HashMap<String, Map<GroundedAction, Double>>();
	private static final Map<String, Double> agentFactors = new HashMap<String, Double>();
	private static final Random random = new Random();
	
	public static void setAgentFactors(Map<String, Double> factors) {
		AgentHelper.agentFactors.putAll(factors);
	}
	
	public static double getActionTime(GroundedAction action) {
		String agentName = (action.params[0].equals("human")) ? "human" : "other";
		Map<GroundedAction, Double> times = AgentHelper.actionTimes.get(agentName);
		if (times == null) {
			times = new HashMap<GroundedAction, Double>();
			AgentHelper.actionTimes.put(agentName, times);
		}
		Double time = times.get(action);
		if (time == null) {
			double factor = AgentHelper.agentFactors.get(agentName);
			time = factor * AgentHelper.random.nextDouble();
			times.put(action, time);
		}
		return times.get(action);
	}
	
	public static double computeSequenceTime(List<AbstractGroundedAction> actions) {
		double time = 0.0;
		for (AbstractGroundedAction action : actions) {
			time += AgentHelper.getActionTime((GroundedAction)action);
		}
		return time;	
	}
	
	public static double computeCompleteSequenceTime(List<AbstractGroundedAction> actions) {
		return 0.0;
	}
	public static List<Recipe> recipes(Domain domain) {
		return Arrays.asList(
				CoffeeWithMilk.getRecipe(domain),
				CerealWithMilk.getRecipe(domain),
				Pancake.getRecipe(domain),
				FriedEgg.getRecipe(domain),
				ScrambledEgg.getRecipe(domain),
				ChocolateMilk.getRecipe(domain),
				Tea.getRecipe(domain)
				//Brownies.getRecipe(domain), 
				//ChocolateChipCookies.getRecipe(domain), 
				//CranberryWalnutCookies.getRecipe(domain), 
				//MoltenLavaCake.getRecipe(domain),
				//(Recipe)PeanutButterCookies.getRecipe(domain)
				);
	}
	
	public static List<KitchenSubdomain> generateAllRTDPPoliciesParallel(Domain generalDomain, State startingState, List<Recipe> recipes, RewardFunction rf, StateHashFactory hashingFactory) {
		
		ForEachCallable<Recipe, List<KitchenSubdomain>> callable = new RecipePlanningCallable(generalDomain, startingState, rf, hashingFactory);
		List<List<KitchenSubdomain>> policyDomains = Parallel.ForEach(recipes, callable);
		List<KitchenSubdomain> flattened = new ArrayList<KitchenSubdomain>();
		for (List<KitchenSubdomain> list : policyDomains) flattened.addAll(list);
		return flattened;
		/*for (int i = 0; i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			policyDomains.addAll(AgentHelper.generateRTDPPolicies(recipe, generalDomain, startingState, rf, hashingFactory));
		}*/
		
		//return policyDomains;
	}
	
public static List<KitchenSubdomain> generateAllRTDPPolicies(Domain generalDomain, State startingState, List<Recipe> recipes, RewardFunction rf, StateHashFactory hashingFactory) {
		
		List<KitchenSubdomain> flattened = new ArrayList<KitchenSubdomain>();
		for (int i = 0; i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			flattened.addAll(AgentHelper.generateRTDPPolicies(recipe, generalDomain, startingState, rf, hashingFactory));
		}
		
		return flattened;
	}

public static State generateActionSequence(KitchenSubdomain currentSubgoal, List<KitchenSubdomain> remainingSubgoals, State startingState, RewardFunction rf, List<GroundedAction> actions, boolean finishRecipe) {
	Set<KitchenSubdomain> active = new HashSet<KitchenSubdomain>();
	Set<KitchenSubdomain> completed = new HashSet<KitchenSubdomain>();
	
	active.add(currentSubgoal);
	
	State nextState = startingState;
	while (!active.isEmpty()) {
		Iterator<KitchenSubdomain> iterator = active.iterator();
		KitchenSubdomain current = iterator.next();
		iterator.remove();
		nextState = AgentHelper.generateActionSequence(current, nextState, rf, actions);
		completed.add(current);
		if (!finishRecipe){
			break;
		}
		
		for (KitchenSubdomain subdomain : remainingSubgoals) {
			if (subdomain.getSubgoal().allPreconditionsCompleted(startingState) && !completed.contains(subdomain)) {
				active.add(subdomain);
			}
		}
	}
	
	return AgentHelper.generateFinalState(startingState, actions);
}
	
	public static State generateActionSequence(List<KitchenSubdomain> remainingSubgoals, State startingState, RewardFunction rf, List<GroundedAction> actions, boolean finishRecipe) {
		Set<KitchenSubdomain> active = new HashSet<KitchenSubdomain>();
		Set<KitchenSubdomain> completed = new HashSet<KitchenSubdomain>();
		
		for (KitchenSubdomain subdomain : remainingSubgoals) {
			if (subdomain.getSubgoal().allPreconditionsCompleted(startingState)) {
				active.add(subdomain);
			}
		}
		
		State nextState = startingState;
		while (!active.isEmpty()) {
			Iterator<KitchenSubdomain> iterator = active.iterator();
			KitchenSubdomain current = iterator.next();
			iterator.remove();
			nextState = AgentHelper.generateActionSequence(current, nextState, rf, actions);
			completed.add(current);
			
			for (KitchenSubdomain subdomain : remainingSubgoals) {
				if (subdomain.getSubgoal().allPreconditionsCompleted(startingState) && !completed.contains(subdomain)) {
					active.add(subdomain);
				}
			}
		}
		
		return AgentHelper.generateFinalState(startingState, actions);
	}
	
	public static State generateActionSequence(KitchenSubdomain policyDomain, State startingState, RewardFunction rf, List<GroundedAction> actions) {
		BakingSubgoal subgoal = policyDomain.getSubgoal();
		Policy policy = policyDomain.getPolicy();
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(subgoal.getGoal());
		policyDomain.getPlanner().planFromState(startingState);
		EpisodeAnalysis episodeAnalysis = policy.evaluateBehavior(startingState, rf, recipeTerminalFunction, 100);
		/*
		System.out.println("Action list for " + policyDomain.toString());
		for (GroundedAction action : episodeAnalysis.actionSequence) {
			System.out.println("\t" + action.actionName() + " " + Arrays.toString(action.params) );
		}
		System.out.println("");
		*/
		actions.addAll(episodeAnalysis.actionSequence);
		
		return AgentHelper.generateFinalState(startingState, actions);
}
	
	public static State generateFinalState(State start, List<GroundedAction> actions) {
		State next = start;
		for (GroundedAction action : actions) {
			next = action.executeIn(next);
		}
		return next;
	}
	
	public static List<GroundedAction> generateRecipeActionSequence(State startState, RewardFunction rf, List<KitchenSubdomain> policyDomains) {
		List<GroundedAction> actions = new ArrayList<GroundedAction>();
		State currentState = startState;
		AgentHelper.generateActionSequence(policyDomains, currentState, rf, actions, true);
		return actions;
	}
	
	public static List<KitchenSubdomain> generateRTDPPolicies(Recipe recipe, Domain domain, State state, RewardFunction rf, StateHashFactory hashingFactory) {
		// Add our actions to the domain.
		
		List<KitchenSubdomain> policyDomains = new ArrayList<KitchenSubdomain>();
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		
		//System.out.println("\n\nPlanner will now plan the "+recipe.topLevelIngredient.getName()+" recipe!");
		
		// High level planner that plans through the recipe's subgoals
		Set<BakingSubgoal> activeSubgoals = new HashSet<BakingSubgoal>();
		
		
		do {
			// For all subgoals with all preconditions satisfied
			for (BakingSubgoal sg : activeSubgoals) {
				subgoals.remove(sg);
				//System.out.println("Planning subgoal " + sg.getIngredient().getName());
				KitchenSubdomain subdomain = AgentHelper.generateRTDPPolicy(domain, state, recipe, sg, rf, hashingFactory);
				
				state = ExperimentHelper.getEndState(subdomain);
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
	
	

	public static KitchenSubdomain generateRTDPPolicy(Domain domain, State startingState, Recipe recipe, BakingSubgoal subgoal, RewardFunction rf, StateHashFactory hashingFactory)
	{
		IngredientRecipe ingredient = subgoal.getIngredient();
		//String goalType = (subgoal.getGoal().getClass().isAssignableFrom(ContainersCleaned.class)) ? "_clean" : "";
		//System.out.println(ingredient.getName() + goalType);
		State currentState = new State(startingState);
		
		// Add the current top level ingredient so we can properly trim the action space
		domain = AgentHelper.setSubgoal(domain, subgoal);
		AffordanceCreator theCreator = new AffordanceCreator(domain, currentState, ingredient);
		
		final PropositionalFunction isSuccess = subgoal.getGoal();
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess);
		
		int numRollouts = 1000; // RTDP
		int maxDepth = 10; // RTDP
		double vInit = 0;
		double maxDelta = .01;
		double gamma = 0.99;
		
		boolean affordanceMode = true;
		AffordanceRTDP planner;
		Policy p;
		AffordancesController affController = theCreator.getAffController();
		// RTDP planner that also uses affordances to trim action space during the Bellman update
		planner = new BellmanAffordanceRTDP(domain, rf, recipeTerminalFunction, gamma, hashingFactory, vInit, 
				numRollouts, maxDelta, maxDepth, affController);
		planner.toggleDebugPrinting(false);
		planner.planFromState(currentState);
		
		// Create a Q-greedy policy from the planner
		p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);

		//AgentHelper.printPlan(subgoal, p, currentState, rf, recipeTerminalFunction);
		
		
		return KitchenSubdomain.makeSubdomain(domain, recipe, subgoal, startingState, p, planner);
	}
	
	private static void printPlan(BakingSubgoal subgoal, Policy policy, State state, RewardFunction rf, TerminalFunction tf) {
		EpisodeAnalysis ea = policy.evaluateBehavior(state, rf, tf, 100);
		if (ea.getDiscountedReturn(1.0) < -5) {
			System.err.println("Reward too much");
			EpisodeAnalysis ea2 = policy.evaluateBehavior(state, rf, tf, 100);
			if (ea.getDiscountedReturn(1.0) != ea2.getDiscountedReturn(1.0)) {
				System.err.println("Rewards not equal");
				ea = ea2;
			}
		}
		System.out.println("Action sequence for " + subgoal.toString());
		for (AbstractGroundedAction action : ea.actionSequence) {
			System.out.println("\t" + action.toString());
		}
		State lastState = ea.stateSequence.get(ea.stateSequence.size() - 1);
		if (!tf.isTerminal(lastState)) {
			System.err.println("Last state is not terminal");
		}
	}
	
	
	public static Domain setSubgoal(Domain domain, BakingSubgoal subgoal) {
		IngredientRecipe ingredient = subgoal.getIngredient();
		
		SADomain newDomain = new SADomain((SADomain)domain);
		
		List<PropositionalFunction> propFunctions = domain.getPropFunctions();
		List<PropositionalFunction> newPropFunctions = new ArrayList<PropositionalFunction>();
		for (PropositionalFunction pf : propFunctions) {
			BakingPropositionalFunction oldPf = (BakingPropositionalFunction)pf;
			newPropFunctions.add(oldPf.updatePF(newDomain, ingredient, subgoal));
		}
		
		return new SADomain(newDomain, newPropFunctions);
	}
	public static class RecipePlanningCallable extends ForEachCallable<Recipe, List<KitchenSubdomain>> {
		
		Domain generalDomain;
		State startingState;
		Recipe recipe;
		RewardFunction rf;
		StateHashFactory hashingFactory;
		public RecipePlanningCallable(Domain generalDomain, State startingState, RewardFunction rf, StateHashFactory hashingFactory) {
			this.generalDomain = generalDomain;
			this.startingState = startingState;
			this.rf = rf;
			this.hashingFactory = hashingFactory;
		}
		
		public RecipePlanningCallable(RecipePlanningCallable base, Recipe recipe) {
			this.generalDomain = base.generalDomain;
			this.startingState = base.startingState;
			this.rf = base.rf;
			this.hashingFactory = new NameDependentStateHashFactory((NameDependentStateHashFactory)base.hashingFactory);
			this.recipe = recipe;
		}
		
		@Override
		public List<KitchenSubdomain> call() throws Exception {
			long start = System.nanoTime();
			List<KitchenSubdomain> res = AgentHelper.generateRTDPPolicies(this.recipe, this.generalDomain, this.startingState, this.rf, this.hashingFactory);
			long end = System.nanoTime();
			System.out.println("Time " + this.recipe.toString() + " - " + ((double)(end - start)) / 1000000000.0);
			return res;
		}

		@Override
		public ForEachCallable<Recipe, List<KitchenSubdomain>> init(
				Recipe current) {
			return new RecipePlanningCallable(this, current);
		}
		
	}
}
