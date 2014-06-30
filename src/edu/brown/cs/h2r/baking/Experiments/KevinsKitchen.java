package edu.brown.cs.h2r.baking.Experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
//import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.planning.QComputablePlanner;
//import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
//import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
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

public class KevinsKitchen implements DomainGenerator {
	List<ObjectInstance> allIngredients;
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
		
		return domain;
	}
	
	public void PlanRecipeOneAgent(Domain domain, Recipe recipe) {
		// Add our actions to the domain.
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		//Action melt = new MeltAction(domain, recipe.topLevelIngredient);
		Action peel = new PeelAction(domain, recipe.topLevelIngredient);
		Action grease = new GreaseAction(domain);
		Action a_switch = new SwitchAction(domain);
		//Action bake = new BakeAction(domain, recipe.topLevelIngredient);
		State state = new State();
		
		// Get the "highest" subgoal in our recipe.
		if (this.topLevelIngredient == null) {
			this.topLevelIngredient = recipe.topLevelIngredient;
		}
		
		recipe.setUpSubgoals(domain);
		
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));

		state.addObject(ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_dish", null, "counter"));
		state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter"));
		state.addObject(SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, ""));
		state.addObject(SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, ""));
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		IngredientKnowledgebase knowledgebase = new IngredientKnowledgebase();
		this.allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, recipe.topLevelIngredient);
	
		System.out.println("\n\nPlanner will now plan the "+recipe.topLevelIngredient.getName()+" recipe!");
		((PourAction)pour).addAllIngredients(this.allIngredients);
		
		Set<BakingSubgoal> subgoals = recipe.getSubgoals();
		BakingSubgoal completed = null;
		while (!subgoals.isEmpty()) {
			for (BakingSubgoal sg : subgoals) {
				if (sg.allPreconditionsCompleted(state)) {
					state = this.PlanIngredient(domain, state, sg.getIngredient(), sg);
					completed = sg;
					break;
				}
			}
			subgoals.remove(completed);
			for (BakingSubgoal sg : subgoals) {
				BakingSubgoal todelete = null;
				for (BakingSubgoal pc : sg.getPreconditions()) {
					if (pc.equals(completed)) {
						todelete = pc;
						break;
					}
				}
				if (todelete != null) {
					sg.getPreconditions().remove(todelete);
				}
			}
		}
		
		//this.PlanIngredient(domain, state, recipe.topLevelIngredient);
	}
	
	public State PlanIngredient(Domain domain, State startingState, IngredientRecipe ingredient, BakingSubgoal subgoal)
	{
		System.out.println(""+ingredient.getName());
		State currentState = new State(startingState);
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = currentState.getObject("counter");

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
			}
		}
		
		for (Action action : domain.getActions()) {
			((BakingAction)action).changePlanningIngredient(ingredient);
		}
		AffordanceCreator theCreator = new AffordanceCreator(domain, currentState, ingredient);
		// Add the current top level ingredient so we can properly trim the action space
		for (PropositionalFunction pf : domain.getPropFunctions()) {
			((BakingPropositionalFunction)pf).changeTopLevelIngredient(ingredient);
			((BakingPropositionalFunction)pf).setSubgoal(subgoal);
		}
		final PropositionalFunction isSuccess = subgoal.getGoal();
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		RewardFunction rf = new RewardFunction() {
			@Override
			public double reward(State s, GroundedAction a, State sprime) {
				if (isFailure.isTrue(sprime, a.params[a.params.length-1])) {
					return -10;
				}
				String success_name = isSuccess.getClassName();
				if (success_name.equals(AffordanceCreator.FINISH_PF)) {
					if (isSuccess.isTrue(sprime, a.params[a.params.length-1])) {
						return 10;
					}
				} else if (success_name.equals(AffordanceCreator.CONTAINERGREASED_PF)
							 && a.actionName().equals(GreaseAction.className)) {
					if (isSuccess.isTrue(sprime, a.params)) {
						return 10;
					}
						
				} else if (success_name.equals(AffordanceCreator.SPACEON_PF) &&
						a.actionName().equals(SwitchAction.className)){
					if (isSuccess.isTrue(sprime, a.params)) {
						return 10;
					}
					
				}
				return -1;
			}
		};
		
		//List<State> reachableStates = StateReachability.getReachableStates(currentState, domain, hashFactory);
		//System.out.println("Number of reachable states: " + reachableStates.size());
		
		// Trying out new stuff!
		int numRollouts = 2000; // RTDP
		int maxDepth = 20; // RTDP
		double vInit = 0;
		double maxDelta = .01;
		double gamma = 1;
		
		boolean affordanceMode = true;
		RTDP planner;
		Policy p;
		AffordancesController affController = theCreator.getAffController();
		if(affordanceMode) {
			planner = new AffordanceRTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
			planner.setMinNumRolloutsWithSmallValueChange(100);
			planner.toggleDebugPrinting(false);
			planner.planFromState(currentState);
			
			// Create a Q-greedy policy from the planner
			p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);
		} else {
			planner = new RTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth);
			planner.setMinNumRolloutsWithSmallValueChange(30);
			planner.planFromState(currentState);
			
			// Create a Q-greedy policy from the planner
			p = new GreedyQPolicy((QComputablePlanner)planner);
		}
		
		// Print out the planning results
		EpisodeAnalysis episodeAnalysis = p.evaluateBehavior(currentState, rf, recipeTerminalFunction,100);

		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);

		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
		
		ExperimentHelper.checkIngredientCompleted(ingredient, endState, finalObjects, containerObjects);
		
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
		//kitchen.PlanRecipeOneAgent(domain, new Brownies());
		//kitchen.PlanRecipeOneAgent(domain, new DeviledEggs());
		//kitchen.PlanRecipeOneAgent(domain, new CucumberSalad());
		//kitchen.PlanRecipeOneAgent(domain, new MashedPotatoes());
		kitchen.PlanRecipeOneAgent(domain, new MoltenLavaCake());
		//kitchen.PlanRecipeOneAgent(domain, new PeanutButterCookies());
		//kitchen.PlanRecipeOneAgent(domain, new PecanPie());
	}
}
