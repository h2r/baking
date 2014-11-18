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
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainersCleaned;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.Recipes.BlueberryMuffins;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Recipes.RecipeActionParameters;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.HandAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PeelAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class BaxterKitchen {
	List<ObjectInstance> allIngredients;
	List<BakingSubgoal> ingSubgoals;
	RecipeActionParameters recipeParams;
	private IngredientRecipe topLevelIngredient;
	public BaxterKitchen() {
		Domain domain = this.generateDomain();
		recipeParams = new RecipeActionParameters(domain);
	}
	
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
		
		Action mix = new MixAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		Action peel = new PeelAction(domain);
		Action turnOnOff = new SwitchAction(domain);
		Action use = new UseAction(domain);
		//Action waitAction = new WaitAction(domain);

		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	public State generateInitialState(Domain domain, Recipe recipe) {
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		// Get the "highest" subgoal in our recipe.
		if (this.topLevelIngredient == null) {
			this.topLevelIngredient = recipe.topLevelIngredient;
		}
		
		objects.add(AgentFactory.getNewHumanAgentObjectInstance(domain, "human", null));
		objects.add(AgentFactory.getNewRobotAgentObjectInstance(domain, "baxter", null));
		
		//objects.add(MakeSpanFactory.getNewObjectInstance(domain, "make_span", 2));
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish"/*, "melting_pot"*/);
		ObjectInstance counterSpace = SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_COUNTER, containers, "human", null);
		//objects.add(counterSpace);
		//objects.add(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_ROBOT, containers, "baxter"));

		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_dish", null, SpaceFactory.SPACE_COUNTER, null));
		//state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, SpaceFactory.SPACE_COUNTER));
		//objects.add(SpaceFactory.getNewBakingSpaceObjectInstance(domain, SpaceFactory.SPACE_OVEN, null, ""));
		//objects.add(SpaceFactory.getNewCleaningSpaceObjectInstance(domain, SpaceFactory.SPACE_SINK, null, "baxter"));
		
		//objects.add(ToolFactory.getNewSimpleToolObjectInstance(domain, "whisk", "", "", SpaceFactory.SPACE_ROBOT));
		//objects.add(ToolFactory.getNewSimpleToolObjectInstance(domain, "spoon","", "", SpaceFactory.SPACE_ROBOT));
		
		for (String container : containers) { 
			objects.add(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER, null));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(domain);
		this.allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, null, recipe);
		//knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
	
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		
		List<ObjectInstance> containersAndIngredients = 
				Recipe.getContainersAndIngredients(containerClass, this.allIngredients, counterSpace.getName());
		
		objects.addAll(containersAndIngredients);

		return new State(objects);
	}
	
	public List<Policy> generatePolicies(Domain domain, Recipe recipe) {
		this.ingSubgoals = recipe.createIngredientSubgoals();
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
		String goalType = (subgoal.getGoal().getClass().isAssignableFrom(ContainersCleaned.class)) ? "_clean" : "";
		System.out.println(ingredient.getName() + goalType);
		State currentState = new State(startingState);
		
		List<Action> actions = domain.getActions();
		AffordanceCreator theCreator = new AffordanceCreator(domain, currentState, ingredient);
		// Add the current top level ingredient so we can properly trim the action space
		domain = AgentHelper.setSubgoal(domain, subgoal);

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
		
		int numRollouts = 300; // RTDP
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
		AffordanceCreator theCreator = new AffordanceCreator(domain, currentState, ingredient);
		// Add the current top level ingredient so we can properly trim the action space
		domain = AgentHelper.setSubgoal(domain, subgoal);
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
		//ExperimentHelper.printResults(episodeAnalysis.actionSequence, episodeAnalysis.rewardSequence);
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
		
		Map<String, List<String>> toolSubgoalLookup = new HashMap<String, List<String>>();
		toolSubgoalLookup.put("wet_ingredients", Arrays.asList("whisk"));
		toolSubgoalLookup.put("dry_ingredients", Arrays.asList("spoon"));
		toolSubgoalLookup.put("brownie_batter", Arrays.asList("whisk"));
		
		
		
		
		List<BakingSubgoal> likelySubgoals = new ArrayList<BakingSubgoal>();
		
		//System.out.println("Likely subgoals:");
		for (BakingSubgoal subgoal : subgoals) {
			if (subgoal.allPreconditionsCompleted(state) && !subgoal.goalCompleted(state)) {
				likelySubgoals.add(subgoal);
				String goalType = (subgoal.getGoal().getClass().isAssignableFrom(ContainersCleaned.class)) ? "_clean" : "";
				//System.out.println(subgoal.getIngredient().getName() + goalType);
			}
		}
		//System.out.println("");
		
		
		
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
				//System.out.println(containerName + ": " + containerSpace);
				if (containerSpace.equals(SpaceFactory.SPACE_ROBOT) ||
						containerSpace.equals(SpaceFactory.SPACE_SINK)){
					subgoalProbability += 1.0 / containers.size();
				}
				allContainersInSink &= containerSpace.equals(SpaceFactory.SPACE_SINK);
			}
			
			List<ObjectInstance> tools = state.getObjectsOfTrueClass(ToolFactory.ClassName);
			for (ObjectInstance tool : tools) {
				List<String> subgoalTools = toolSubgoalLookup.get(subgoal.getIngredient().getName());
				
				if ( subgoalTools.contains(tool.getName())) {
					String toolSpace = ToolFactory.getSpaceName(tool);
					allContainersInSink &= toolSpace.equals(SpaceFactory.SPACE_COUNTER);
				}
				
			}
			
			String goalType = (subgoal.getGoal().getClass().isAssignableFrom(ContainersCleaned.class)) ? "_clean" : "";
			if (!allContainersInSink && subgoalProbability > maxProbability) {
				//System.out.println(subgoal.getIngredient().getName() + goalType + ": " + subgoalProbability);
				maxProbability = subgoalProbability;
				likelySubgoal = subgoal;
			}
		}
		 //System.out.println("");

		return likelySubgoal;
	}
	
	public List<String> getContainersForSubgoal(Domain domain, State state, BakingSubgoal subgoal) {
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(domain);
		List<IngredientRecipe> ingredients = knowledgebase.getPotentialIngredientList(domain, subgoal.getIngredient());
		List<String> containers = new ArrayList<String>();
		
		for (IngredientRecipe ingredient : ingredients) {
			if (ingredient.isSimple()) {
				containers.add(ingredient.getName() + "_bowl");
			}
		}
		
		containers.add((subgoal.getIngredient().getName().equals("dry_ingredients")) ? "mixing_bowl_1" : "mixing_bowl_2");
		
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
		ObjectInstance object = state.getObject(objectName);
		if (object.getTrueClassName().equals(ContainerFactory.ClassName)) {
			System.out.println("Move " + object.getName() + " to sink");
			ContainerFactory.changeContainerSpace(object, SpaceFactory.SPACE_SINK);
		}
		else {
			if (object.getTrueClassName().equals(ToolFactory.ClassName)) {
				if (!ToolFactory.isUsed(object)) {
					System.out.println("Move " + object.getName() + " to counter");
					ContainerFactory.changeContainerSpace(object, SpaceFactory.SPACE_COUNTER);
				}
				if (ToolFactory.isUsed(object)) {
					System.out.println("Move " + object.getName() + " to sink");
					ContainerFactory.changeContainerSpace(object, SpaceFactory.SPACE_SINK);
				}
			}
		}
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
		ObjectInstance object = state.getObject(name);
		if (object == null) {
			System.out.println(name + " does not exist in the robot's state space");
			return state;
		}
		String className = object.getObjectClass().name;
		if (!Arrays.asList(ContainerFactory.ClassName, ToolFactory.ClassName).contains(className)) {
			return state;
		}
		if (Arrays.asList("flour_bowl", "cocoa_bowl", "salt_bowl", "baking_powder_bowl").contains(name)) {
			state = this.takeAction(domain, state, "pour", "human", name, "mixing_bowl_1");
			state = this.takeAction(domain, state, "move", "baxter", name, SpaceFactory.SPACE_ROBOT);
		} else if (Arrays.asList("eggs_bowl", "vanilla_bowl", "butter_bowl", "white_sugar_bowl").contains(name)) {
			state = this.takeAction(domain, state, "pour", "human", name, "mixing_bowl_2");
			state = this.takeAction(domain, state, "move", "baxter", name, SpaceFactory.SPACE_ROBOT);
		} else if (name.equals("mixing_bowl_1")) {
			state = this.takeAction(domain, state, "pour", "human", name, "mixing_bowl_2");
			state = this.takeAction(domain, state, "move", "baxter", name, SpaceFactory.SPACE_ROBOT);
			
			
		} else if (name.equals("mixing_bowl_2")) {
			state = this.takeAction(domain, state, "pour", "human", name, "baking_dish");
			state = this.takeAction(domain, state, "move", "baxter", name, SpaceFactory.SPACE_ROBOT);
		} else if (Arrays.asList("whisk").contains(name)) {
			state = this.takeAction(domain, state, "mix", "human", "mixing_bowl_2", name);			
			state = this.takeAction(domain, state, "hand", "baxter", name, SpaceFactory.SPACE_ROBOT);
		} else if (Arrays.asList( "spoon").contains(name)) {
			state = this.takeAction(domain, state, "mix", "human", "mixing_bowl_1", name);
			state = this.takeAction(domain, state, "hand", "baxter", name, SpaceFactory.SPACE_ROBOT);
		}
		
		/*else if (ContainerFactory.isMixingContainer(object)) {
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
		//else {
		//	System.out.println("Succeeded: " + actionName + ", " + Arrays.toString(params));
		//}
		
		return groundedAction.executeIn(state);
	}
	
	public void testRecipeExecution(Domain domain, Recipe recipe) {
		State state = this.generateInitialState(domain, recipe);
		List<String[]> actionParams = this.recipeParams.getRecipeParams(recipe.getRecipeName());
		List<BakingSubgoal> subgoals = recipe.getSubgoals();
		for (String[] params : actionParams) {
			System.out.println(Arrays.toString(params));
			State previousState = state.copy();
			
			
			state = this.takeAction(domain, state, params[0], Arrays.copyOfRange(params, 1, params.length));
			System.out.println((previousState.equals(state)) ? "Failure":"Success");
			for (BakingSubgoal subgoal : subgoals) {
				
				
				IngredientRecipe ing = subgoal.getIngredient();
				
				domain = AgentHelper.setSubgoal(domain, subgoal);
				
				BakingPropositionalFunction pf = subgoal.getGoal();
				
				List<BakingSubgoal> preconditions = subgoal.getPreconditions();
				//boolean checkSubgoal = true;
				for (BakingSubgoal precondition : preconditions) {
					IngredientRecipe ing2 = precondition.getIngredient();
					BakingPropositionalFunction pf2 = precondition.getGoal();
					//checkSubgoal &=  pf2.isTrue(state, "");
					System.out.println("Precondition " + ing2.getName() + ": " + pf2.isTrue(state, ""));
					
				}
				//if (checkSubgoal)
				String isClean = (pf instanceof ContainersCleaned) ? "_clean" : "";
				System.out.println("Subgoal " + ing.getName() + isClean + ": " + pf.isTrue(state, ""));				
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		BaxterKitchen kitchen = new BaxterKitchen();
		Domain domain = TestManyAgents.generateGeneralDomain();
		
		//Recipe recipe = new BlueberryMuffins(domain);
		kitchen.testRecipeExecution(domain, Brownies.getRecipe(domain));
		
		//List<Policy> policies = kitchen.generatePolicies(domain, brownies);
	}
}
