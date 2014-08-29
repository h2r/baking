package edu.brown.cs.h2r.baking.Experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
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
import burlap.oomdp.singleagent.common.UniformCostRF;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.BellmanAffordanceRTDP;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
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
import edu.brown.cs.h2r.baking.actions.BakingActionResult;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;

public class KevinsKitchen {
	List<ObjectInstance> allIngredients;
	List<BakingSubgoal> ingSubgoals;
	private IngredientRecipe topLevelIngredient;
	public KevinsKitchen() {

	}
	
	public Domain generateDomain(Recipe recipe) {
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
		
		return domain;
	}
	
	public void testRecipeExecution(Domain domain, Recipe recipe) {
		Map<String, Action> actionMap = new HashMap<String, Action>();
		for (Action action : domain.getActions()) {
			actionMap.put(action.getName(), action);
		}
		State state = this.generateInitialState(domain, recipe);
		List<String[]> actionParams = Arrays.asList(
				new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},
				new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
				new String[] {"pour", "human", "white_sugar_bowl", "mixing_bowl_1"},
				new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
				new String[] {"mix", "human", "mixing_bowl_1"},
				
				new String[] {"pour", "human", "flour_bowl", "mixing_bowl_2"},
				new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
				new String[] {"pour", "human", "baking_powder_bowl", "mixing_bowl_2"},
				new String[] {"pour", "human", "cocoa_bowl", "mixing_bowl_2"},

				new String[] {"mix", "human", "mixing_bowl_2"},
				new String[] {"pour", "human", "mixing_bowl_1", "mixing_bowl_2"},
				new String[] {"mix", "human", "mixing_bowl_2"}
				
				);
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		for (String[] params : actionParams) {
			state = this.takeAction(state, params, actionMap);
			for (BakingSubgoal subgoal : subgoals) {
				
				
				IngredientRecipe ing = subgoal.getIngredient();
				
				BakingPropositionalFunction pf = subgoal.getGoal();
				pf.changeTopLevelIngredient(ing);
				pf.setSubgoal(subgoal);
				
				List<BakingSubgoal> preconditions = subgoal.getPreconditions();
				boolean checkSubgoal = true;
				for (BakingSubgoal precondition : preconditions) {
					IngredientRecipe ing2 = precondition.getIngredient();
					BakingPropositionalFunction pf2 = precondition.getGoal();
					checkSubgoal &=  pf2.isTrue(state, "");
					System.out.println("Precondition " + ing2.getName() + ": " + pf2.isTrue(state, ""));
					
				}
				if (checkSubgoal)
				System.out.println("Subgoal " + ing.getName() + ": " + pf.isTrue(state, ""));
				
				
				
			}
		}
	}
	
	public State takeAction(State state, String[] actionParams, Map<String, Action> actionMap) {
		Action action = actionMap.get(actionParams[0]);
		String[] params = Arrays.copyOfRange(actionParams, 1, actionParams.length);
		BakingAction bakingAction = (BakingAction)action;
		BakingActionResult result = bakingAction.checkActionIsApplicableInState(state, params);
		if (!result.getIsSuccess()){
			System.err.println("Action " + actionParams[0] + " is not applicable with params " + Arrays.toString(params));
			System.err.println(result.getWhyFailed());
		}
		
		GroundedAction ga = new GroundedAction(action, params);
		return action.performAction(state, params);
	}
	
	
	public State generateInitialState(Domain domain, Recipe recipe) {
		State state = new State();
		
		// Get the "highest" subgoal in our recipe.
		if (this.topLevelIngredient == null) {
			this.topLevelIngredient = recipe.topLevelIngredient;
		}
		
		recipe.setUpSubgoals(domain);
		// creates ingredient-only subgoals 
		recipe.addIngredientSubgoals();
		recipe.addRequiredRecipeAttributes();
		recipe.setUpRecipeToolAttributes();
		
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_COUNTER, containers, "human"));

		
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		IngredientKnowledgebase knowledgebase = new IngredientKnowledgebase();
		this.allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, recipe.topLevelIngredient.getContents().get(0));
		
		for (ObjectInstance ingredientInstance : this.allIngredients) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = state.getObject(SpaceFactory.SPACE_COUNTER);

		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, this.allIngredients, counterSpace.getName());
		
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, counterSpace.getName());
				state.addObject(containerInstance);
			}
		}

		for (ObjectInstance ingredientInstance : this.allIngredients) {
			if (IngredientFactory.getUseCount(ingredientInstance) >= 1) {
				ObjectInstance ing = state.getObject(ingredientInstance.getName());
				IngredientFactory.changeIngredientContainer(ing, ing.getName()+"_bowl");
				ContainerFactory.addIngredient(state.getObject(ing.getName()+"_bowl"), ing.getName());
				SpaceFactory.addContainer(state.getObject(SpaceFactory.SPACE_COUNTER), state.getObject(ing.getName()+"_bowl"));
			}
		}
		
		return state;
	}
	
	public void PlanRecipeOneAgent(Domain domain, Recipe recipe) {
		// Add our actions to the domain.
		
		State state = this.generateInitialState(domain, recipe);
		
		
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
		
		//((AllowUsingTool)domain.getPropFunction(AffordanceCreator.USE_PF)).addRecipe(recipe);
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		RewardFunction rf = new RewardFunction() {

			@Override
			public double reward(State s, GroundedAction a, State sprime) {
				if (isFailure.isTrue(s,"")) {
					return -10;
				}
				return -1;
			}
			
		};
		
		int numRollouts = 4000; // RTDP
		int maxDepth = 10; // RTDP
		double vInit = 0;
		double maxDelta = .01;
		double gamma = 0.99;
		
		boolean affordanceMode = true;
		OOMDPPlanner planner;
		Policy p;
		AffordancesController affController = theCreator.getAffController();
		if(affordanceMode) {
			// RTDP planner that also uses affordances to trim action space during the Bellman update
			planner = new BellmanAffordanceRTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
			planner.toggleDebugPrinting(true);
			planner.planFromState(currentState);
			
			// Create a Q-greedy policy from the planner
			p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);

		} else {
			//planner = new RTDP(domain, rf, recipeTerminalFunction, gamma, hashFactory, vInit, numRollouts, maxDelta, maxDepth);
			//planner.planFromState(currentState);
			
			// Create a Q-greedy policy from the planner
			//p = new GreedyQPolicy((QComputablePlanner)planner);
			
			StateConditionTest gc = new StateConditionTest() {

				@Override
				public boolean satisfies(State s) {
					return isSuccess.isTrue(s, "");
				}
				
			};
			planner = new BFS(domain, gc, hashFactory);
			planner.setTf(recipeTerminalFunction);
			planner.planFromState(currentState);
			
			p = new DDPlannerPolicy((DeterministicPlanner)planner);
			
			
		}
		
		// Print out the planning results
		EpisodeAnalysis episodeAnalysis = p.evaluateBehavior(currentState, rf, recipeTerminalFunction,100);

		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);

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
		Recipe recipe = new Brownies();
		Domain domain = kitchen.generateDomain(recipe);
		kitchen.testRecipeExecution(domain, recipe);
		//kitchen.PlanRecipeOneAgent(domain, new MashedPotatoes());
		kitchen.PlanRecipeOneAgent(domain, recipe);
		//kitchen.PlanRecipeOneAgent(domain, new DeviledEggs());
		//kitchen.PlanRecipeOneAgent(domain, new CucumberSalad());
		//kitchen.PlanRecipeOneAgent(domain, new MoltenLavaCake());
		//kitchen.PlanRecipeOneAgent(domain, new PeanutButterCookies());
		//kitchen.PlanRecipeOneAgent(domain, new PecanPie());
	}
}
