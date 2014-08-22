package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.UniformCostRF;
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
	
	private static int numRollouts = 300; // RTDP
	private static int maxDepth = 10; // RTDP
	private static double vInit = 0;
	private static double maxDelta = .01;
	private static double gamma = 0.99;
	
	private static boolean affordanceMode = true;
	private static RewardFunction rf = new UniformCostRF();
	private StateHashFactory hashFactory;
	
	private static String agentClass = "robot";
	private static IngredientKnowledgebase knowledgebase = new IngredientKnowledgebase();
	private Recipe recipe;
	private Domain domain;
	private List<Action> domainActions;
	private List<PropositionalFunction> domainPFs;
	
	public Baxter(Domain domain, Recipe recipe, StateHashFactory hashFactory) {
		this.recipe = recipe;
		this.domain = domain;
		this.domainActions = domain.getActions();
		this.domainPFs = domain.getPropFunctions();
		this.hashFactory = hashFactory;
		this.addSubgoalsToPF();
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
				
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		while (!activeSubgoals.isEmpty()) {
			for (BakingSubgoal goal : activeSubgoals) {
				state = this.planIngredient(actionSequence, state, ingredients, goal.getIngredient(), goal);
			}
			activeSubgoals = this.getActiveSubgoalsInState(state, recipe.getSubgoals());
		}
		
		return this.getFirstViableActionInState(beginningState, actionSequence);
	}

	protected void addSubgoalsToPF() {
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)this.domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
			for (BakingSubgoal goal : this.recipe.getIngredientSubgoals()) {
				failed.addSubgoal(goal);
			}
		}
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
		
		AffordanceCreator theCreator = new AffordanceCreator(domain, currentState, ingredient);
		AffordancesController affordanceController = theCreator.getAffController();
		this.setupForPlan(ingredient, subgoal, currentState);
		
		// TODO I don't think this is quite doing the optimal thing in regards to multiple agents.
		final PropositionalFunction isSuccess = subgoal.getGoal();
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);

		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);

		
		OOMDPPlanner planner;
		if(Baxter.affordanceMode) {
			// RTDP planner that also uses affordances to trim action space during the Bellman update
			planner = new BellmanAffordanceRTDP(this.domain, Baxter.rf, recipeTerminalFunction, Baxter.gamma, this.hashFactory, 
					Baxter.vInit, Baxter.numRollouts, Baxter.maxDelta, Baxter.maxDepth, affordanceController);
			//planner.toggleDebugPrinting(false);
		} else {
			planner = new RTDP(this.domain, Baxter.rf, recipeTerminalFunction, Baxter.gamma, this.hashFactory, 
					Baxter.vInit, Baxter.numRollouts, Baxter.maxDelta, Baxter.maxDepth);
		}
		planner.planFromState(currentState);
		
		Policy p = (Baxter.affordanceMode) ? 
				new AffordanceGreedyQPolicy(affordanceController, (QComputablePlanner)planner) :
					new GreedyQPolicy((QComputablePlanner)planner);

		
		// Print out the planning results
		EpisodeAnalysis episodeAnalysis = p.evaluateBehavior(currentState, rf, recipeTerminalFunction,100);
		actionSequence.addAll(episodeAnalysis.actionSequence);
		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
		
		this.updateState(ingredient, endState);
		
		System.out.println(episodeAnalysis.getActionSequenceString(" \n"));
		ExperimentHelper.printResults(episodeAnalysis.actionSequence, episodeAnalysis.rewardSequence);
		
		//if (subgoal.getGoal().getClassName().equals(AffordanceCreator.FINISH_PF)) {
		//	IngredientFactory.hideUnecessaryIngredients(endState, domain, ingredient, ingredients);
		//}
		
		return endState;
	}

	protected void updateState(IngredientRecipe ingredient, State endState) {
		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
		
		ExperimentHelper.makeSwappedIngredientObject(ingredient, endState, finalObjects, containerObjects);
	}

	protected void setupForPlan(IngredientRecipe ingredient,
			BakingSubgoal subgoal, State currentState) {
		for (Action action : this.domainActions) {
			((BakingAction)action).changePlanningIngredient(ingredient);
		}
		
		// Add the current top level ingredient so we can properly trim the action space
		for (PropositionalFunction pf : this.domainPFs) {
			((BakingPropositionalFunction)pf).changeTopLevelIngredient(ingredient);
			((BakingPropositionalFunction)pf).setSubgoal(subgoal);
		}
		subgoal.getGoal().changeTopLevelIngredient(ingredient);
	}

}
