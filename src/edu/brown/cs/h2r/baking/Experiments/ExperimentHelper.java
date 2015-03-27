package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Recipes.RecipeActionParameters;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;

public class ExperimentHelper {

	public ExperimentHelper() {
	}
	
	public static void printEpisodeSequence(List<GroundedAction> fullActions,
			List<Double> fullReward) {
		for (int i =0 ; i < fullActions.size(); ++i) {
			GroundedAction action = fullActions.get(i);
			
			double reward = -999;
			if (i < fullReward.size()) {
			reward = fullReward.get(i);
			}
			System.out.print("Cost: " + reward + " " + action.action.getName() + " ");
			for (int j = 0; j < action.params.length; ++j) {
				System.out.print(action.params[j] + " ");
			}
			System.out.print("\n");
		}
	}

	public static void printResults(List<GroundedAction> actions, List<Double> rewards) {
		double reward = 0;
		for (double r : rewards) {
			reward += r;
		}
		
		int numRobotActions = 0;
		int numHumanActions = 0;
		
		for (GroundedAction ga : actions) {
			if (ga.params[0] == "robot") {
				numRobotActions++;
			}
			else {
				numHumanActions++;
			}
		}
		
		System.out.println("Reward: " + reward);
		System.out.println("Human: " + numHumanActions);
		System.out.println("Robot: " + numRobotActions);
	}
	
	public static ObjectInstance getNewNamedComplexIngredient(ObjectInstance unnamedIngredient, String name)
	{
		return IngredientFactory.getNewIngredientInstance(unnamedIngredient, name);
	}
	
	public static GroundedAction getRandomGroundedAction(Domain domain, State currentState, String agent) {
		List<Action> actions = domain.getActions();
		
		List<GroundedAction> gas = new ArrayList<GroundedAction>();
		for (Action action : actions) {
			List<GroundedAction> groundedActions = action.getAllApplicableGroundedActions(currentState);
			gas.addAll(groundedActions);
		}
		Random r = new Random();
		
		GroundedAction ga = gas.get(r.nextInt(gas.size()));
		
		while (ga != null && ga.params[0] != agent)
		{
			ga = gas.get(r.nextInt(gas.size()));
		}
		return ga;
	}
	
	public static boolean isGroundedActionApplicableInState(State state, GroundedAction action)
	{
		return action.action.applicableInState(state, action.params);
	}

	public static int numberActionsApplicableInState(State currentState, GroundedAction action1, GroundedAction action2, boolean reverse)
	{
		GroundedAction firstAction = (reverse) ? action2 : action1;
		GroundedAction secondAction = (reverse) ? action1 : action2;
		
		int count = 0;
		State nextState = currentState.copy();
		
		if (firstAction != null) {
			count += ExperimentHelper.isGroundedActionApplicableInState(currentState, firstAction) ? 1 : 0;
			nextState = firstAction.executeIn(currentState);
		}
		if (secondAction != null) {
			if (firstAction == null || (firstAction.params[1] != secondAction.params[1] &&
					ExperimentHelper.isGroundedActionApplicableInState(nextState, secondAction))) {
				count++;
			}
		}
		return count;
	}
	
	public static State applyGroundedActions(State currentState, GroundedAction action1, GroundedAction action2, boolean reverse)
	{
		GroundedAction firstAction = (reverse) ? action2 : action1;
		GroundedAction secondAction = (reverse) ? action1 : action2;
		
		State nextState = currentState.copy();
		if (firstAction != null) {
			nextState = firstAction.executeIn(currentState);
		}
		if (secondAction != null) {
			if (firstAction == null || (firstAction.params[1] != secondAction.params[1] &&
					ExperimentHelper.isGroundedActionApplicableInState(nextState, secondAction))) {
				nextState = secondAction.executeIn(nextState);
			}
		}
		return nextState;
	}
	
	public static State makeSwappedIngredientObject(IngredientRecipe ingredient,
			State endState, List<ObjectInstance> finalObjects,
			List<ObjectInstance> containerObjects) {
		ObjectInstance namedIngredient;
		
		for (ObjectInstance obj : finalObjects)
		{
			if (ingredient.isMatching(obj, endState))
			{
				namedIngredient = ExperimentHelper.getNewNamedComplexIngredient(obj, ingredient.getFullName());
				namedIngredient = IngredientFactory.changeSwapped(namedIngredient);
				String container = IngredientFactory.getContainer(obj);
				namedIngredient = IngredientFactory.changeIngredientContainer(namedIngredient, container);
				
				ObjectInstance containerInstance = endState.getObject(container);
				ObjectInstance newContainer = ContainerFactory.removeContents(containerInstance);
				newContainer = ContainerFactory.addIngredient(newContainer, ingredient.getFullName());
				
				return endState.replaceAllObjects(Arrays.asList(containerInstance, obj), Arrays.asList(newContainer, namedIngredient));
			}
		}
		return endState;
	}
	
	public static State setPrimaryAgent(State state, String agent)
	{
		State newState = state.copy();
		List<ObjectInstance> makeSpanObjects = newState.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty())
		{
			ObjectInstance makeSpanObject = makeSpanObjects.get(0);
			MakeSpanFactory.setPrimaryAgent(makeSpanObject, agent);
		}
		return newState;
	}
	
	public static GroundedAction getFirstRelavantAction(List<GroundedAction> actions, String agent)
	{
		for (GroundedAction action : actions) {
			if (action.params[0] == agent) {
				return action;
			}
		}
		return null;
	}
	
	public static State takeAction(Domain domain, State state, GroundedAction action ) {
		
		BakingActionResult result = ((BakingAction)action.action).checkActionIsApplicableInState(state, action.params);
		if (!result.getIsSuccess()) {
			System.err.println(result.getWhyFailed());
		}
		
		return action.executeIn(state);
	}
	
	private static List<List<ObjectInstance>> getChangedObjects(State old, State newState) {
		List<ObjectInstance> removedObjects = new ArrayList<ObjectInstance>();
		List<ObjectInstance> addedObjects = new ArrayList<ObjectInstance>();
		
		List<ObjectInstance> oldObjects = old.getAllObjects();
		List<ObjectInstance> newObjects = newState.getAllObjects();
		
		for (ObjectInstance oldObject : oldObjects) {
			Integer index =  newObjects.indexOf(oldObject);
			ObjectInstance replacement = (index >= 0) ? newObjects.get(index) : null;
			if (replacement == null || !oldObject.valueEquals(replacement)) {
				removedObjects.add(oldObject);
			}
		}
		
		for (ObjectInstance newObject : newObjects) {
			Integer index =  oldObjects.indexOf(newObject);
			ObjectInstance replacement = (index >= 0) ? oldObjects.get(index) : null;
			
			if (replacement == null || !newObject.valueEquals(replacement)) {
				addedObjects.add(newObject);
			}
		}
		
		return Arrays.asList(removedObjects, addedObjects);
	}
	
	public static void testRecipeExecution(Domain domain, State state, Recipe recipe,
			boolean printActionResults, boolean printStateDiffs, boolean printPreconditions) {
		int actionResults = 41;
		int stateDiffs = 42;
		int preconds = 43;
		DPrint.toggleCode(actionResults, printActionResults);
		DPrint.toggleCode(stateDiffs, printStateDiffs);
		DPrint.toggleCode(preconds, printPreconditions);
		RecipeActionParameters recipeParams = new RecipeActionParameters(domain);
		List<String[]> actionParams = recipeParams.getRecipeParams(recipe.getRecipeName());
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		Map<String, Boolean> previousValues = new HashMap<String, Boolean>();
		Set<BakingSubgoal> completedSubgoals = new HashSet<BakingSubgoal>();
		for (String[] params : actionParams) {
			DPrint.cl(actionResults, Arrays.toString(params));
			State previousState = state.copy();
			if (params[0].equals("mix")) {
				//System.out.print("");
			}
			Action action = domain.getAction(params[0]);			
			GroundedAction groundedAction = new GroundedAction(action, Arrays.copyOfRange(params, 1, params.length));
			
			boolean oneRecommended = 
					checkSubgoalRecommendations(domain, state, actionResults, subgoals, completedSubgoals, groundedAction);
			if (!oneRecommended) {
				System.err.println("No subgoal recommended this action");
				oneRecommended = checkSubgoalRecommendations(domain, state, actionResults, subgoals, completedSubgoals, groundedAction);
			}
			
			state = takeAction(domain, state, groundedAction);
			List<List<ObjectInstance>> changedObjects = ExperimentHelper.getChangedObjects(previousState, state);
			
			String successString = (previousState.equals(state)) ? "failure":"success";
			DPrint.cl(actionResults, "Action was a " + successString);
			//System.out.println("Recipe failure " + recipe.isFailure(state));
			DPrint.cl(actionResults, "Recipe success " + recipe.isSuccess(state));
			List<ObjectInstance> removedObjects = changedObjects.get(0);
			List<ObjectInstance> addedObjects = changedObjects.get(1);
			
			DPrint.cl(stateDiffs, "Removed");
			for (ObjectInstance object : removedObjects) {
				String oldStr = object.toString();
				oldStr = oldStr.replace("\n", "\n\t");
				DPrint.cl(stateDiffs, oldStr);
			}
			
			DPrint.cl(stateDiffs, "Added");
			for (ObjectInstance object : addedObjects) {
				String oldStr = object.toString();
				oldStr = oldStr.replace("\n", "\n\t");
				DPrint.cl(stateDiffs, oldStr);
			}
			
			for (BakingSubgoal subgoal : subgoals) {
				
				
				IngredientRecipe ing = subgoal.getIngredient();
				
				BakingPropositionalFunction pf = subgoal.getGoal();
				domain = AgentHelper.setSubgoal(domain, subgoal);
				
				
				List<BakingSubgoal> preconditions = subgoal.getPreconditions();
				String pfType = pf.toString();
				for (GroundedProp prop : pf.getAllGroundedPropsForState(state)) {
					String subgoalString = "Subgoal " + ing.getFullName() + " - " + pfType + " " + Arrays.toString(prop.params);
					Boolean currentValue = prop.isTrue(state);
					Boolean previousValue = previousValues.get(subgoalString);
					if (previousValue == null || previousValue != currentValue) {
						DPrint.cl(actionResults, "    " + subgoalString + ": " + currentValue);
					}
					previousValues.put(subgoalString, currentValue);
					if (currentValue) {
						completedSubgoals.add(subgoal);
					}
				}
				
				for (BakingSubgoal precondition : preconditions) {
					IngredientRecipe ing2 = precondition.getIngredient();
					BakingPropositionalFunction pf2 = precondition.getGoal();
					pfType = pf2.toString();
					for (GroundedProp prop : pf2.getAllGroundedPropsForState(state)) {
						String preconditionString = "Precondition " + ing2.getFullName() + " - " + pfType + " " + Arrays.toString(prop.params);
						Boolean currentValue = prop.isTrue(state);
						Boolean previousValue = previousValues.get(preconditionString);
						if (previousValue == null || previousValue != currentValue) {
							DPrint.cl(preconds, "      " + preconditionString + ": " + currentValue);
						}
						previousValues.put(preconditionString, currentValue);
					}
				}
				
			}
			
			
		}
		System.out.println("Recipe success " + recipe.isSuccess(state));
	}

	private static boolean checkSubgoalRecommendations(Domain domain,
			State state, int actionResults, List<BakingSubgoal> subgoals,
			Set<BakingSubgoal> completedSubgoals, GroundedAction groundedAction) {
		
		List<BakingSubgoal> recommendingSubgoals = new ArrayList<BakingSubgoal>();
		for (BakingSubgoal subgoal : subgoals) {
			if (completedSubgoals.contains(subgoal)) {
				continue;
			}
			domain = AgentHelper.setSubgoal(domain, subgoal);
			AffordanceCreator theCreator = new AffordanceCreator(domain, state, subgoal.getIngredient());
			AffordancesController affController = theCreator.getAffController();
			Boolean recommended = affController.isActionRecommendedInState(state, groundedAction);
			if (recommended) {
				recommendingSubgoals.add(subgoal);
			}
		}
		DPrint.cl(actionResults, "Recommending subgoals");
		for (BakingSubgoal subgoal : recommendingSubgoals) {
			DPrint.cl(actionResults, "\t" + subgoal.toString());
		}
		return !recommendingSubgoals.isEmpty();
	}

	public static State getEndState(KitchenSubdomain policyDomain) {
		BakingSubgoal subgoal = policyDomain.getSubgoal();
		Domain domain = policyDomain.getDomain();
		State startingState = policyDomain.getStartState();
		AffordanceRTDP planner = policyDomain.getPlanner();
		planner.planFromState(startingState);
		GreedyQPolicy policy = new GreedyQPolicy(planner);
		PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(subgoal.getGoal(), isFailure);
		RewardFunction rf = new RecipeAgentSpecificMakeSpanRewardFunction("human");
		EpisodeAnalysis episodeAnalysis = policy.evaluateBehavior(startingState, rf, recipeTerminalFunction,100);
		
		
		for (GroundedAction action : episodeAnalysis.actionSequence) {
			//System.out.println("\t" + action.actionName() + " " + Arrays.toString(action.params) );
		}
		
		return episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
		
	}
	
	public static void printPlan(KitchenSubdomain policyDomain, RewardFunction rf) {
		BakingSubgoal subgoal = policyDomain.getSubgoal();
		Domain domain = policyDomain.getDomain();
		Policy policy = policyDomain.getPolicy();
		AffordanceRTDP planner = policyDomain.getPlanner();
		State startingState = policyDomain.getStartState();
		PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(subgoal.getGoal(), isFailure);
		planner.planFromState(startingState);
		EpisodeAnalysis episodeAnalysis = policy.evaluateBehavior(startingState, rf, recipeTerminalFunction,100);
		
		System.out.println(policyDomain.toString());
		for (GroundedAction action : episodeAnalysis.actionSequence) {
			System.out.println("\t" + action.actionName() + " " + Arrays.toString(action.params) );
		}
		
	}
}
