package edu.brown.cs.h2r.baking.Experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import edu.brown.cs.h2r.baking.Knowledgebase.ToolKnowledgebase;
import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.ObjectFactories.*;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.*;
import edu.brown.cs.h2r.baking.actions.*;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;

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
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		//Action use = new UseAction(domain, recipe.topLevelIngredient);
		Action peel = new PeelAction(domain, recipe.topLevelIngredient);
		State state = new State();
		
		// Get the "highest" subgoal in our recipe.
		if (this.topLevelIngredient == null) {
			this.topLevelIngredient = recipe.topLevelIngredient;
		}
		
		recipe.setUpSubgoals(domain);
		// creates ingredient-only subgoals 
		recipe.addIngredientSubgoals();
		recipe.addRequiredRecipeAttributes();
		
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
		
		// Get the tools!
		ToolKnowledgebase toolKnowledgebase = new ToolKnowledgebase();
		for (Entry<String, String[]> tool : toolKnowledgebase.getToolMap().entrySet()) {
			String name = tool.getKey();
			String[] toolInfo = tool.getValue();
			String toolTrait = toolInfo[0];
			String toolAttribute = toolInfo[1];
			if (toolInfo.length == 3) {
				state.addObject(ToolFactory.getNewCarryingToolObjectInstance(domain, name, toolTrait, toolAttribute, SpaceFactory.SPACE_COUNTER));
			} else {
				state.addObject(ToolFactory.getNewSimpleToolObjectInstance(domain, name, toolTrait, toolAttribute, SpaceFactory.SPACE_COUNTER));
			}
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		IngredientKnowledgebase knowledgebase = new IngredientKnowledgebase();
		this.allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, recipe.topLevelIngredient);
	
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
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = currentState.getObject(SpaceFactory.SPACE_COUNTER);

		List<ObjectInstance> ingredientInstances = this.allIngredients;
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances, counterSpace.getName());
		
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (currentState.getObject(ingredientInstance.getName()) == null) {
				currentState.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (currentState.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, counterSpace.getName());
				currentState.addObject(containerInstance);
			}
		}

		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (IngredientFactory.getUseCount(ingredientInstance) >= 1) {
				ObjectInstance ing = currentState.getObject(ingredientInstance.getName());
				IngredientFactory.changeIngredientContainer(ing, ing.getName()+"_bowl");
				ContainerFactory.addIngredient(currentState.getObject(ing.getName()+"_bowl"), ing.getName());
				SpaceFactory.addContainer(currentState.getObject(SpaceFactory.SPACE_COUNTER), currentState.getObject(ing.getName()+"_bowl"));
			}
		}
		
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
	}
	
	public static void main(String[] args) throws IOException {
		
		KevinsKitchen kitchen = new KevinsKitchen();
		Domain domain = kitchen.generateDomain();
		kitchen.PlanRecipeOneAgent(domain, new MashedPotatoes());
		kitchen.PlanRecipeOneAgent(domain, new Brownies());
		kitchen.PlanRecipeOneAgent(domain, new DeviledEggs());
		kitchen.PlanRecipeOneAgent(domain, new CucumberSalad());
		kitchen.PlanRecipeOneAgent(domain, new MoltenLavaCake());
		kitchen.PlanRecipeOneAgent(domain, new PeanutButterCookies());
		kitchen.PlanRecipeOneAgent(domain, new PecanPie());
	}
}
