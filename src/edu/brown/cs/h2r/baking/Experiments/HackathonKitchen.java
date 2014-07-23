package edu.brown.cs.h2r.baking.Experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import edu.brown.cs.h2r.baking.BellmanAffordanceRTDP;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.ObjectFactories.*;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.*;
import edu.brown.cs.h2r.baking.actions.*;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;

public class HackathonKitchen implements DomainGenerator {
	List<ObjectInstance> allIngredients;
	List<BakingSubgoal> ingSubgoals;
	private IngredientRecipe topLevelIngredient;
	public HackathonKitchen() {

	}
	
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.setObjectIdentiferDependence(true);
		return domain;
	}
	/*public void PlanHackathon(Domain domain, State state, Recipe recipe) {
		recipe.setUpSubgoals(domain);
		// creates ingredient-only subgoals 
		recipe.addIngredientSubgoals();
		recipe.addRequiredRecipeAttributes();
		
		System.out.println("\n\nPlanner will now plan the "+recipe.topLevelIngredient.getName()+" recipe!");
		
		// High level planner that plans through the recipe's subgoals
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		Set<BakingSubgoal> activeSubgoals = new HashSet<BakingSubgoal>();
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		this.ingSubgoals = recipe.getIngredientSubgoals();
		
		do {
			// For all subgoals with all preconditions satisfied
			for (BakingSubgoal sg : activeSubgoals) {
				subgoals.remove(sg);
				state = this.PlanIngredient(domain, state, sg.getIngredient(), sg);
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
	}
	
	public State PlanIngredient(Domain domain, State startingState, IngredientRecipe ingredient, BakingSubgoal subgoal)
	{
		System.out.println(ingredient.getName());
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
		
		if (((RecipeBotched)isFailure).hasNoSubgoals()) {
			for (BakingSubgoal sg : this.ingSubgoals) {
				((RecipeBotched)isFailure).addSubgoal(sg);
			}
		}		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		RewardFunction rf = new RewardFunction() {
			@Override
			// Uniform cost function for an optimistic algorithm that guarantees convergence.
			public double reward(State state, GroundedAction a, State sprime) {
				return -1;
			}
		};
		
		int numRollouts = 2000; // RTDP
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
		
		// Print out the planning results
		EpisodeAnalysis episodeAnalysis = p.evaluateBehavior(currentState, rf, recipeTerminalFunction,100);

		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
		//System.out.println("Succeeded : " + recipeTerminalFunction.isTerminal(endState));

		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
		
		ExperimentHelper.makeSwappedIngredientObject(ingredient, endState, finalObjects, containerObjects);
		
		System.out.println(episodeAnalysis.getActionSequenceString(" \n"));
		ExperimentHelper.printResults(episodeAnalysis.actionSequence, episodeAnalysis.rewardSequence);
		
		if (subgoal.getGoal().getClassName().equals(AffordanceCreator.FINISH_PF)) {
			IngredientFactory.hideUnecessaryIngredients(endState, domain, ingredient, this.allIngredients);
		}
		return endState;
	}*/
	public State PlanHackathon(Domain domain, State state, Recipe recipe) {
		// High level planner that plans through the recipe's subgoals
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		Set<BakingSubgoal> activeSubgoals = new HashSet<BakingSubgoal>();
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		this.ingSubgoals = recipe.getIngredientSubgoals();
		
		do {
			// For all subgoals with all preconditions satisfied
			for (BakingSubgoal sg : activeSubgoals) {
				//subgoals.remove(sg);
				state = this.planOneAction(domain, state, sg.getIngredient(), sg);
				return state;
			}
			//activeSubgoals.clear();
			// Iterate through inactive subgoals to find those who have had all of their
			// preconditions resolved.
			for (BakingSubgoal sg : subgoals) {
				if (sg.allPreconditionsCompleted(state)) {
					activeSubgoals.add(sg);
				}
			}	
		} while (!activeSubgoals.isEmpty());
		return state;
	}
	public State planOneAction(Domain domain, State startingState, IngredientRecipe ingredient, BakingSubgoal subgoal)
	{
		System.out.println("\nPlanning subgoal: " + ingredient.getName());
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
		final PropositionalFunction bowlsClean = domain.getPropFunction(AffordanceCreator.CLEAN_PF);
		
		if (((RecipeBotched)isFailure).hasNoSubgoals()) {
			for (BakingSubgoal sg : this.ingSubgoals) {
				((RecipeBotched)isFailure).addSubgoal(sg);
			}
		}		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(bowlsClean, isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		RewardFunction rf = new RewardFunction() {
			@Override
			// Uniform cost function for an optimistic algorithm that guarantees convergence.
			public double reward(State state, GroundedAction a, State sprime) {
				return -1;
			}
		};
		
		int numRollouts = 2000; // RTDP
		int maxDepth = 10; // RTDP
		double vInit = 0;
		double maxDelta = .01;
		double gamma = 0.99;
		
		boolean affordanceMode = true;
		RTDP planner;
		Policy p;
		AffordancesController affController = theCreator.getAffController();
	
		// RTDP planner that also uses affordances to trim action space during the Bellman update
		planner = new BellmanAffordanceRTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
		planner.toggleDebugPrinting(false);
		planner.planFromState(currentState);
		
		// Create a Q-greedy policy from the planner
		p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);
	
		// Print out the planning results
		EpisodeAnalysis episodeAnalysis = p.evaluateBehavior(currentState, rf, recipeTerminalFunction,100);

		System.out.println(episodeAnalysis.getAction(0).toString());
		State endState = episodeAnalysis.getState(1);
		//System.out.println("Succeeded : " + recipeTerminalFunction.isTerminal(endState));

		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
		
		ExperimentHelper.makeSwappedIngredientObject(ingredient, endState, finalObjects, containerObjects);
		return endState;
	}
	
	public void addAllIngredients(Collection<ObjectInstance> ings) {
		this.allIngredients = new ArrayList<ObjectInstance>(ings);
	}
}
