package edu.brown.cs.h2r.baking.Experiments;

import java.io.IOException;
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
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
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
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
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
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class KevinsKitchen implements DomainGenerator {
	List<ObjectInstance> allIngredients;
	List<BakingSubgoal> ingSubgoals;
	private IngredientRecipe topLevelIngredient;
	public KevinsKitchen() {

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
		domain.addObjectClass(ToolFactory.createObjectClass(domain));
		domain.setObjectIdentiferDependence(true);
		return domain;
	}
	
	public void PlanRecipeOneAgent(Domain domain, Recipe recipe) {
		// Add our actions to the domain.
		Action mix = new MixAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain);
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		// Get the "highest" subgoal in our recipe.
		if (this.topLevelIngredient == null) {
			this.topLevelIngredient = recipe.topLevelIngredient;
		}
		
		
		objects.add(AgentFactory.getNewHumanAgentObjectInstance(domain, "human", null));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish", "melting_pot");
		//objects.add(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_COUNTER, containers, "human"));

		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_dish", null, SpaceFactory.SPACE_COUNTER));
		objects.add(ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, SpaceFactory.SPACE_COUNTER));
		//objects.add(SpaceFactory.getNewBakingSpaceObjectInstance(domain, SpaceFactory.SPACE_OVEN, null, ""));
		//objects.add(SpaceFactory.getNewHeatingSpaceObjectInstance(domain, SpaceFactory.SPACE_STOVE, null, ""));
		
		for (String container : containers) { 
			objects.add(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(domain);
		//this.allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		
		
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		

		List<ObjectInstance> ingredientsAndContainers = 
				Recipe.getContainersAndIngredients(containerClass, this.allIngredients, SpaceFactory.SPACE_COUNTER);
		
		objects.addAll(ingredientsAndContainers);
		//objects.addAll(knowledgebase.getTools(domain, SpaceFactory.SPACE_COUNTER));
		
		State state = new State(objects);
		
		
		System.out.println("\n\nPlanner will now plan the "+recipe.topLevelIngredient.getFullName()+" recipe!");
		
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
		System.out.println(ingredient.getFullName());
		
		// TODO you should have startingState be set before this method.
		List<Action> actions = domain.getActions();
		AffordanceCreator theCreator = new AffordanceCreator(domain, startingState, ingredient);
		// Add the current top level ingredient so we can properly trim the action space
		domain = AgentHelper.setSubgoal(domain, subgoal);
		final PropositionalFunction isSuccess = subgoal.getGoal();
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		if (((RecipeBotched)isFailure).hasNoSubgoals()) {
			for (BakingSubgoal sg : this.ingSubgoals) {
				((RecipeBotched)isFailure).addSubgoal(sg);
			}
		}
		
		//((AllowUsingTool)domain.getPropFunction(AffordanceCreator.USE_PF)).addRecipe(recipe);
		
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
		//affController.setCurrentGoal(isSuccess);
		affController.setCurrentGoal(theCreator.getPFAtom(subgoal.getGoal().getName()));
		if(affordanceMode) {
			// RTDP planner that also uses affordances to trim action space during the Bellman update
			planner = new BellmanAffordanceRTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
			//planner.toggleDebugPrinting(false);
			planner.planFromState(startingState);
			
			// Create a Q-greedy policy from the planner
			p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);

		} else {
			planner = new RTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth);
			planner.planFromState(startingState);
			
			// Create a Q-greedy policy from the planner
			p = new GreedyQPolicy((QComputablePlanner)planner);
		}
		
		// Print out the planning results
		EpisodeAnalysis episodeAnalysis = p.evaluateBehavior(startingState, rf, recipeTerminalFunction,100);

		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);

		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
		
		ExperimentHelper.makeSwappedIngredientObject(ingredient, endState, finalObjects, containerObjects);
		
		System.out.println(episodeAnalysis.getActionSequenceString(" \n"));
		ExperimentHelper.printResults(episodeAnalysis.actionSequence, episodeAnalysis.rewardSequence);
		
		if (subgoal.getGoal().getClassName().equals(AffordanceCreator.FINISH_PF)) {
			endState = IngredientFactory.hideUnecessaryIngredients(endState, domain, ingredient, this.allIngredients);
		}
		
		return endState;
	}
	
	public static void main(String[] args) throws IOException {
		
		KevinsKitchen kitchen = new KevinsKitchen();
		Domain domain = kitchen.generateDomain();
		kitchen.PlanRecipeOneAgent(domain, Brownies.getRecipe(domain));
	}
}
