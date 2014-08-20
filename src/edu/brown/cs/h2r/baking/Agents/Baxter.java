package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
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
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.BellmanAffordanceRTDP;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Experiments.ExperimentHelper;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.BakingAction;

public class Baxter implements Agent {
	
	private static String agentClass = "robot";
	private static IngredientKnowledgebase knowledgebase = new IngredientKnowledgebase();
	private Recipe recipe;
	private Domain domain;
	
	public Baxter(Domain domain, Recipe recipe) {
		this.recipe = recipe;
		this.domain = domain;
	}
		
	@Override
	public AbstractGroundedAction getAction(State state) {
		if (this.recipe != null) {
			return this.getActionForCurrentRecipe(state);
		}
		return null;
	}
	
	public boolean canPerformActionInState(GroundedAction action, State state) {
		String[] params = action.params;
		params[0] = Baxter.agentClass;
		return action.action.applicableInState(state, params);
	}
	
	public AbstractGroundedAction getActionForCurrentRecipe(State state)
	{
		State beginningState = new State(state);
		List<ObjectInstance> ingredients = 
				Baxter.knowledgebase.getPotentialIngredientObjectInstanceList(
						state, this.domain, this.recipe.topLevelIngredient);
	
		// Find actionable subgoals
		List<BakingSubgoal> activeSubgoals = this.getActiveSubgoalsInState(beginningState, recipe.getSubgoals());
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)this.domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
			for (BakingSubgoal goal : this.recipe.getIngredientSubgoals()) {
				failed.addSubgoal(goal);
			}
		}
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		for (BakingSubgoal goal : activeSubgoals) {
			state = this.planIngredient(actionSequence, state, ingredients, goal.getIngredient(), goal);
		}
		
		return this.getFirstViableActionInState(beginningState, actionSequence);
	}
	
	protected List<BakingSubgoal> getActiveSubgoalsInState(State state, List<BakingSubgoal> allSubgoals)
	{
		List<BakingSubgoal> activeSubgoals = new ArrayList<BakingSubgoal>();
		for (BakingSubgoal goal : allSubgoals) {
			if (goal.allPreconditionsCompleted(state)) {
				activeSubgoals.add(goal);
			}
		}
		return activeSubgoals;
	}
	
	protected AbstractGroundedAction getFirstViableActionInState(State state, List<AbstractGroundedAction> actionSequence)
	{
		GroundedAction groundedAction;
		for (AbstractGroundedAction abstractAction : actionSequence)
		{
			groundedAction = (GroundedAction)abstractAction;
			if (this.canPerformActionInState(groundedAction, state)) {
				return abstractAction;
			}
		}
		return null;
	}
	
	protected State planIngredient(List<AbstractGroundedAction> actionSequence, State startingState, List<ObjectInstance> ingredients, 
			IngredientRecipe ingredient, BakingSubgoal subgoal)
	{
		System.out.println(ingredient.getName());
		State currentState = new State(startingState);
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = currentState.getObject(SpaceFactory.SPACE_COUNTER);

		List<ObjectInstance> ingredientInstances = ingredients;
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
			PropositionalFunction pf = subgoal.getGoal();
			
			PFAtom goalAtom = new PFAtom((PropositionalFunction)subgoal.getGoal());
			affController.setCurrentGoal(goalAtom);
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
		actionSequence.addAll(episodeAnalysis.actionSequence);
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
			IngredientFactory.hideUnecessaryIngredients(endState, domain, ingredient, ingredients);
		}
		
		return endState;
	}

}
