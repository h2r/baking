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
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.BellmanAffordanceRTDP;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.HandAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class BaxterKitchen {
	List<ObjectInstance> allIngredients;
	List<BakingSubgoal> ingSubgoals;
	private IngredientRecipe topLevelIngredient;
	public BaxterKitchen() {

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
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain, recipe.topLevelIngredient);
		Action hand = new HandAction(domain, recipe.topLevelIngredient);
		//Action waitAction = new WaitAction(domain);

		recipe.init(domain);
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		this.ingSubgoals = recipe.getIngredientSubgoals();

		
		return domain;
	}
	
	public State generateInitialState(Domain domain, Recipe recipe) {
		State state = new State();
		
		// Get the "highest" subgoal in our recipe.
		if (this.topLevelIngredient == null) {
			this.topLevelIngredient = recipe.topLevelIngredient;
		}
		
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		state.addObject(AgentFactory.getNewRobotAgentObjectInstance(domain, "baxter"));
		
		state.addObject(MakeSpanFactory.getNewObjectInstance(domain, "make_span", 2));
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2"/*, "baking_dish", "melting_pot"*/);
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_COUNTER, containers, "human"));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_ROBOT, containers, "baxter"));

		state.addObject(ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_dish", null, SpaceFactory.SPACE_COUNTER));
		//state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, SpaceFactory.SPACE_COUNTER));
		state.addObject(SpaceFactory.getNewBakingSpaceObjectInstance(domain, SpaceFactory.SPACE_OVEN, null, ""));
		state.addObject(SpaceFactory.getNewCleaningSpaceObjectInstance(domain, SpaceFactory.SPACE_SINK, null, "baxter"));
		
		state.addObject(ToolFactory.getNewSimpleToolObjectInstance(domain, "whisk", "", "", SpaceFactory.SPACE_ROBOT));
		state.addObject(ToolFactory.getNewSimpleToolObjectInstance(domain, "spoon","", "", SpaceFactory.SPACE_ROBOT));
		
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = new Knowledgebase();
		this.allIngredients = knowledgebase.getRecipeObjectInstanceList(state, domain, recipe);
		//knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
	
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
			if (!IngredientFactory.isHiddenIngredient(ingredientInstance)) {
				if (IngredientFactory.getUseCount(ingredientInstance) >= 1) {
					ObjectInstance ing = state.getObject(ingredientInstance.getName());
					IngredientFactory.changeIngredientContainer(ing, ing.getName()+"_bowl");
					ContainerFactory.addIngredient(state.getObject(ing.getName()+"_bowl"), ing.getName());
					SpaceFactory.addContainer(state.getObject(SpaceFactory.SPACE_COUNTER), state.getObject(ing.getName()+"_bowl"));
				}
			}
		}
		return state;
	}
	
	public List<Policy> generatePolicies(Domain domain, Recipe recipe) {
		// Add our actions to the domain.
		List<Policy> policies = new ArrayList<Policy>();
		State state = this.generateInitialState(domain, recipe);
		System.out.println("\n\nPlanner will now plan the "+recipe.topLevelIngredient.getName()+" recipe!");
		
		// High level planner that plans through the recipe's subgoals
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		Set<BakingSubgoal> activeSubgoals = new HashSet<BakingSubgoal>();
		
		
		do {
			// For all subgoals with all preconditions satisfied
			for (BakingSubgoal sg : activeSubgoals) {
				subgoals.remove(sg);
				state = this.determineAndEvaluatePolicy(domain, state, sg);
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
		return policies;
	}
	
	public Policy generatePolicy(Domain domain, State startingState, BakingSubgoal subgoal)
	{
		IngredientRecipe ingredient = subgoal.getIngredient();
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
		RewardFunction rf = new RecipeAgentSpecificMakeSpanRewardFunction("human");
		/*
				new RewardFunction() {
			@Override
			// Uniform cost function for an optimistic algorithm that guarantees convergence.
			public double reward(State state, GroundedAction a, State sprime) {
				return -1;
			}
		};*/
		
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
		this.evaluatePolicy(domain, currentState, subgoal, p);
		return p;
	}
	
	public State determineAndEvaluatePolicy(Domain domain, State startingState, BakingSubgoal subgoal) {
		IngredientRecipe ingredient = subgoal.getIngredient();
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
		RewardFunction rf = new RecipeAgentSpecificMakeSpanRewardFunction("human");
		
		Policy p = this.generatePolicy(domain, startingState, subgoal);
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
			IngredientFactory.hideUnecessaryIngredients(endState, domain, ingredient, this.allIngredients);
		}
		
		return endState;

	}
	
	public EpisodeAnalysis evaluatePolicy(Domain domain, State state, BakingSubgoal subgoal, Policy policy) {
		final PropositionalFunction isSuccess = subgoal.getGoal();
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		
		if (((RecipeBotched)isFailure).hasNoSubgoals()) {
			for (BakingSubgoal sg : this.ingSubgoals) {
				((RecipeBotched)isFailure).addSubgoal(sg);
			}
		}
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		RewardFunction rf = new RecipeAgentSpecificMakeSpanRewardFunction("human");
		EpisodeAnalysis episodeAnalysis = policy.evaluateBehavior(state, rf, recipeTerminalFunction,100);

		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
				
		System.out.println(episodeAnalysis.getActionSequenceString(" \n"));
		ExperimentHelper.printResults(episodeAnalysis.actionSequence, episodeAnalysis.rewardSequence);
		return episodeAnalysis;
	}
	
	public void setHumanOccupied(State state) {
		ObjectInstance makeSpanObject = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName).get(0);
		MakeSpanFactory.occupyAgent(makeSpanObject, "human");
	}
	
	public String[] getParams(AbstractGroundedAction action) {
		String[] params = new String[action.params.length + 1];
		params[0] = action.actionName();
		for (int i = 0; i < action.params.length; i++) {
			params[i+1] = action.params[i];
		}
		return params;
	}
	
	public String[] getRobotAction(Domain domain, State state, Recipe recipe) {
		this.setHumanOccupied(state);
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		
		BakingSubgoal currentGoal = this.determineSubgoal(domain, state, subgoals);
		if (currentGoal == null) {
			return null;
		}
		Policy policy = this.generatePolicy(domain, state.copy(), currentGoal);
		
		AbstractGroundedAction chosenAction = policy.getAction(state.copy());
		/*if (!chosenAction.params[0].equals("baxter")) {
			chosenAction = null;
			EpisodeAnalysis ea = this.evaluatePolicy(domain, state.copy(), currentGoal, policy);
			for (AbstractGroundedAction aga : ea.actionSequence) {
				GroundedAction ga = (GroundedAction)aga;
				if (ga.action.applicableInState(state.copy(), ga.params) && ga.params[0].equals("baxter")) {
					chosenAction = aga;
				}
			}
		}*/
		return (chosenAction == null || !chosenAction.params[0].equals("baxter")) ? null : this.getParams(chosenAction);
	}
	
	public BakingSubgoal determineSubgoal(Domain domain, State state, List<BakingSubgoal> subgoals) {
		List<BakingSubgoal> likelySubgoals = new ArrayList<BakingSubgoal>();
		for (BakingSubgoal subgoal : subgoals) {
			if (subgoal.allPreconditionsCompleted(state) && !subgoal.goalCompleted(state)) {
				likelySubgoals.add(subgoal);
			}
		}
		
		double maxProbability = 0.0;
		Map<String, ObjectInstance> containerLookup = new HashMap<String, ObjectInstance>();
		for (ObjectInstance container : state.getObjectsOfTrueClass(ContainerFactory.ClassName)) {
			containerLookup.put(container.getName(), container);
		}
		
		if (likelySubgoals.isEmpty()) {
			return null;
		}
		
		BakingSubgoal likelySubgoal = likelySubgoals.get(0);
		for (BakingSubgoal subgoal : likelySubgoals) {
			double subgoalProbability = 0.0;
			List<String> containers = this.getContainersForSubgoal(domain, state, subgoal);
			boolean allContainersInSink = true;
			for (String containerName : containers) {
				ObjectInstance container = containerLookup.get(containerName);
				String containerSpace = ContainerFactory.getSpaceName(container);
				if (containerSpace.equals(SpaceFactory.SPACE_ROBOT) ||
						containerSpace.equals(SpaceFactory.SPACE_SINK)){
					subgoalProbability += 1.0 / containers.size();
				}
				allContainersInSink &= containerSpace.equals(SpaceFactory.SPACE_SINK);
			}
			
			if (!allContainersInSink && subgoalProbability > maxProbability) {
				maxProbability = subgoalProbability;
				likelySubgoal = subgoal;
			}
		}

		return likelySubgoal;
	}
	
	public List<String> getContainersForSubgoal(Domain domain, State state, BakingSubgoal subgoal) {
		Knowledgebase knowledgebase = new Knowledgebase();
		List<IngredientRecipe> ingredients = knowledgebase.getPotentialIngredientList(state, domain, subgoal.getIngredient());
		List<String> containers = new ArrayList<String>();
		
		for (IngredientRecipe ingredient : ingredients) {
			if (ingredient.isSimple()) {
				containers.add(ingredient.getName() + "_bowl");
			}
		}
		
		return containers;
	}
	
	public void addAllFeasibleActions(State state, List<Policy> policies, List<AbstractGroundedAction> actions) {
		for (Policy policy : policies) {
			if (policy.isDefinedFor(state)) {
				//EpisodeAnalysis ea = policy.evaluateBehavior(state, this, tf)
			}
		}
	}
	
	
	public State addObjectInRobotsSpace(Domain domain, State state, String objectName) {
		state = this.takePresumedActions(domain, state, objectName);
		return state;
	}
	
	public State disposeObject(State state, String objectName) {
		ObjectInstance container = state.getObject(objectName);
		ContainerFactory.changeContainerSpace(container, SpaceFactory.SPACE_SINK);
		return state;
	}
	
	public State moveObjectCounter(State state, String objectName) {
		ObjectInstance container = state.getObject(objectName);
		ContainerFactory.changeContainerSpace(container, SpaceFactory.SPACE_COUNTER);
		return state;
	}
		
	/**
	 * Given the name of the new object that has appeared infront of the robot, determine what actions
	 * the human must've take that would've caused the container to appear in front of the robot. Once
	 * said actions have been determined, then update the state such that the robot can later plan on 
	 * this new state that accurately represents the state of the world.
	 */
	public State takePresumedActions(Domain domain, State state, String name) {
		System.out.println("Assumed Actions:");
		ObjectInstance object = state.getObject(name);
		String className = object.getObjectClass().name;
		if (!className.equals(ContainerFactory.ClassName)) {
			return state;
		}
		if (Arrays.asList("flour_bowl", "cocoa_bowl", "salt_bowl", "baking_powder_bowl").contains(name)) {
			state = this.takeAction(domain, state, "pour", "human", name, "mixing_bowl_1");
			state = this.takeAction(domain, state, "move", "baxter", name, SpaceFactory.SPACE_ROBOT);
		} else if (Arrays.asList("eggs_bowl", "vanilla_bowl", "butter_bowl", "white_sugar_bowl").contains(name)) {
			state = this.takeAction(domain, state, "pour", "human", name, "mixing_bowl_2");
			state = this.takeAction(domain, state, "move", "baxter", name, SpaceFactory.SPACE_ROBOT);
		} /*else if (ContainerFactory.isMixingContainer(object)) {
			ObjectInstance wetBowl = state.getObject(ContainerFactory.WET_BOWL);
			ObjectInstance dryBowl = state.getObject(ContainerFactory.DRY_BOWL);
			String wetSpace = ContainerFactory.getSpaceName(wetBowl);
			String drySpace = ContainerFactory.getSpaceName(dryBowl);
			if (name.equals(ContainerFactory.DRY_BOWL)) {
				// If we're moving dry bowl, then we either just poured it into the wet bowl
				// or baking dish. If wetBowl isn't on human space, then we poured it into
				// baking dish, otherwise we didn't!
				if (!wetSpace.equals(SpaceFactory.SPACE_HUMAN)) {
					// poured into Baking dish
					this.takePourAction(new String[] {"human", name, "baking_dish"});
				} else {
					//pouring into wet bowl
					this.takePourAction(new String[] {"human", name, ContainerFactory.WET_BOWL});
				}
				this.takeMoveAction(new String[] {"human", name, SpaceFactory.SPACE_ROBOT});
			} else {
				if (!drySpace.equals(SpaceFactory.SPACE_HUMAN)) {
					// poured into Baking dish
					this.takePourAction(new String[] {"human", name, "baking_dish"});
				} else {
					//pouring into wet bowl
					this.takePourAction(new String[] {"human", name, ContainerFactory.DRY_BOWL});
				}				
				this.takeMoveAction(new String[] {"human", name, SpaceFactory.SPACE_ROBOT});
			}
		} */
		return state;
	}
	
	public State takeAction(Domain domain, State state, String actionName, String ...params ) {
		Action action = domain.getAction(actionName);
		GroundedAction groundedAction = new GroundedAction(action, params);
		BakingActionResult result = ((BakingAction)action).checkActionIsApplicableInState(state, params);
		if (!result.getIsSuccess()) {
			System.err.println(result.getWhyFailed());
		}
		
		return groundedAction.executeIn(state);
	}
	
	public void testRecipeExecution(Domain domain, Recipe recipe) {
		State state = this.generateInitialState(domain, recipe);
		List<String[]> actionParams = Arrays.asList(
				/*new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},*/
				/*new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},*/
				new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
				new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
				new String[] {"pour", "human", "white_sugar_bowl", "mixing_bowl_1"},
				new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
				new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},
				
				new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
				
				new String[] {"pour", "human", "flour_bowl", "mixing_bowl_2"},
				
				new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
				new String[] {"pour", "human", "cocoa_bowl", "mixing_bowl_2"},
				new String[] {"move", "baxter", "cocoa_bowl", SpaceFactory.SPACE_SINK},
				
				/*new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
				new String[] {"pour", "human", "baking_powder_bowl", "mixing_bowl_2"},*/
				
				new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
				
				new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
				new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
				
				new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK},
				
				new String[] {"pour", "human", "mixing_bowl_1", "mixing_bowl_2"},
				new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
				new String[] {"mix", "human", "mixing_bowl_2", "whisk"},
				new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
				
				new String[] {"pour", "human", "mixing_bowl_2", "baking_dish"},
				new String[] {"move", "baxter", "mixing_bowl_2", SpaceFactory.SPACE_SINK},
				
				new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
				new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
				new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK}
				
				
				
				);
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		for (String[] params : actionParams) {
			System.out.println(Arrays.toString(params));
			State previousState = state.copy();
			
			
			state = this.takeAction(domain, state, params[0], Arrays.copyOfRange(params, 1, params.length));
			System.out.println((previousState.equals(state)) ? "Failure":"Success");
			for (BakingSubgoal subgoal : subgoals) {
				
				
				IngredientRecipe ing = subgoal.getIngredient();
				
				BakingPropositionalFunction pf = subgoal.getGoal();
				pf.changeTopLevelIngredient(ing);
				pf.setSubgoal(subgoal);
				
				List<BakingSubgoal> preconditions = subgoal.getPreconditions();
				//boolean checkSubgoal = true;
				for (BakingSubgoal precondition : preconditions) {
					IngredientRecipe ing2 = precondition.getIngredient();
					BakingPropositionalFunction pf2 = precondition.getGoal();
					//checkSubgoal &=  pf2.isTrue(state, "");
					System.out.println("Precondition " + ing2.getName() + ": " + pf2.isTrue(state, ""));
					
				}
				//if (checkSubgoal)
				System.out.println("Subgoal " + ing.getName() + ": " + pf.isTrue(state, ""));
				
				
				
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		BaxterKitchen kitchen = new BaxterKitchen();
		Recipe brownies = new Brownies();
		Domain domain = kitchen.generateDomain(brownies);
		//kitchen.testRecipeExecution(domain, brownies);
		
		//List<Policy> policies = kitchen.generatePolicies(domain, brownies);
		
		
		State state = kitchen.generateInitialState(domain, brownies);
		
		String container = "cocoa_bowl";
		state = kitchen.addObjectInRobotsSpace(domain, state, container);
		String[] action = kitchen.getRobotAction(domain, state, brownies);
		System.out.println(Arrays.toString(action));
		kitchen.disposeObject(state, container);
		System.out.println("");
		
		action = kitchen.getRobotAction(domain, state, brownies);
		System.out.println(Arrays.toString(action));
		System.out.println("");
		state = kitchen.moveObjectCounter(state, "spoon");
		
		container = "flour_bowl";
		state = kitchen.addObjectInRobotsSpace(domain, state, container);
		action = kitchen.getRobotAction(domain, state, brownies);
		System.out.println(Arrays.toString(action));
		kitchen.disposeObject(state, container);
		System.out.println("");
		
		
		container = "butter_bowl";
		state = kitchen.addObjectInRobotsSpace(domain, state, container);
		action = kitchen.getRobotAction(domain, state, brownies);
		System.out.println(Arrays.toString(action));
		kitchen.disposeObject(state, container);
		System.out.println("");
		
		action = kitchen.getRobotAction(domain, state, brownies);
		System.out.println(Arrays.toString(action));
		System.out.println("");
		
		container = "white_sugar_bowl";
		state = kitchen.addObjectInRobotsSpace(domain, state, container);
		action = kitchen.getRobotAction(domain, state, brownies);
		System.out.println(Arrays.toString(action));
		System.out.println("");
		kitchen.disposeObject(state, container);
		
		action = kitchen.getRobotAction(domain, state, brownies);
		System.out.println(Arrays.toString(action));
	}
}
