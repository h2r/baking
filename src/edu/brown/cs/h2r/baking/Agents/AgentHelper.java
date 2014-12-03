package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
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
import edu.brown.cs.h2r.baking.Experiments.ExperimentHelper;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.Experiments.SubgoalDetermination;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Prediction.PolicyPrediction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainersCleaned;
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

public class AgentHelper {
	
	public static List<Recipe> recipes(Domain domain) {
		return Arrays.asList(
				(Recipe)Brownies.getRecipe(domain)/*, 
				new CucumberSalad(), 
				new DeviledEggs(), 
				new MashedPotatoes(),
				new MoltenLavaCake()*/,
				PeanutButterCookies.getRecipe(domain));
	}
	
	public static List<KitchenSubdomain> generateAllRTDPPolicies(Domain generalDomain, State startingState, List<Recipe> recipes, RewardFunction rf, StateHashFactory hashingFactory) {
		List<KitchenSubdomain> policyDomains = new ArrayList<KitchenSubdomain>();
		
		for (int i = 0; i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			policyDomains.addAll(AgentHelper.generateRTDPPolicies(recipe, generalDomain, startingState, rf, hashingFactory));
		}
		
		return policyDomains;
	}
	
	public static State generateActionSequence(KitchenSubdomain policyDomain, State startingState, List<GroundedAction> actions) {
			BakingSubgoal subgoal = policyDomain.getSubgoal();
			Domain domain = policyDomain.getDomain();
			Policy policy = policyDomain.getPolicy();
			
			TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(subgoal.getGoal());
			RewardFunction rf = new RecipeAgentSpecificMakeSpanRewardFunction("human");
			EpisodeAnalysis episodeAnalysis = policy.evaluateBehavior(startingState, rf, recipeTerminalFunction,100);
			
			
			for (GroundedAction action : episodeAnalysis.actionSequence) {
				//System.out.println("\t" + action.actionName() + " " + Arrays.toString(action.params) );
			}
			
			actions.addAll(episodeAnalysis.actionSequence);
			return episodeAnalysis.stateSequence.get(episodeAnalysis.stateSequence.size() - 1);
	}
	
	public static List<GroundedAction> generateRecipeActionSequence(State startState, List<KitchenSubdomain> policyDomains) {
		List<GroundedAction> actions = new ArrayList<GroundedAction>();
		State currentState = startState;
		for (KitchenSubdomain policyDomain : policyDomains) {
			currentState = AgentHelper.generateActionSequence(policyDomain, currentState, actions);
		}
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
		
		int numRollouts = 500; // RTDP
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

		return KitchenSubdomain.makeSubdomain(domain, recipe, subgoal, startingState, p, planner);
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
}
