package edu.brown.cs.h2r.baking;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import edu.brown.cs.h2r.baking.Experiments.HackathonKitchen;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BowlsClean;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.HandAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;

public class KitchenDomain {

	private Domain domain;
	private Recipe recipe;
	private State state;
	private IngredientKnowledgebase knowledgebase;
	private AbstractMap<String, ObjectInstance> allIngredientsMap;
	private HackathonKitchen kitchen;
	private Action mix, pour, move, hand;
	private TerminalFunction tf;
	private RewardFunction rf;
	
	private List<String> bakingDishes = new ArrayList<String>(Arrays.asList("baking_dish"));
	private List<String> mixingBowls = new ArrayList<String>(Arrays.asList(ContainerFactory.DRY_BOWL, ContainerFactory.WET_BOWL));
	private List<String> tools = new ArrayList<String>(Arrays.asList(ToolFactory.SPATULA, ToolFactory.WHISK));
	
	private List<String> dryBowls = new ArrayList<String>(Arrays.asList("baking_dish"));
	
	private AbstractMap<String, String[]> allParams;
	private Set<String> doneActions;
	private static double robotTop = 100;
	private static double robotBottom = 0;
	private static double robotLeft = 0;
	private static double robotRight = 40;
	
	private static double humanTop = 100;
	private static double humanBottom = 0;
	private static double humanLeft = 40;
	private static double humanRight = 80;
	
	private static double dirtyTop = 100;
	private static double dirtyBottom = 0;
	private static double dirtyLeft = 80;
	private static double dirtyRight = 100;
	
	private boolean debug;
	
	public KitchenDomain() {
		// Get the basics
		this.kitchen = new HackathonKitchen();
		this.domain = kitchen.generateDomain();
		this.recipe = new Brownies();
		this.getNewCleanState();
		
		// Add Prop functions and terminal functions needed for planning!
		final PropositionalFunction cleanBowl = new BowlsClean(
				AffordanceCreator.CLEAN_PF, this.domain, this.recipe.topLevelIngredient);
		final RecipeFinished finish = new RecipeFinished(AffordanceCreator.FINISH_PF, this.domain,
				this.recipe.topLevelIngredient);
		final RecipeBotched botched = new RecipeBotched(AffordanceCreator.FINISH_PF, this.domain,
				this.recipe.topLevelIngredient);
		 this.tf = new RecipeTerminalFunction(cleanBowl, finish, botched);
		 this.rf = this.generateRewardFunction();

		// add the actions to the domain
		this.mix = new MixAction(domain, recipe.topLevelIngredient);
		this.pour = new PourAction(domain, recipe.topLevelIngredient);
		this.move = new MoveAction(domain, recipe.topLevelIngredient);
		this.hand = new HandAction(domain, recipe.topLevelIngredient);
		
		// Prepare the recipe (not really used, but needed to avoid crashing
		knowledgebase = new IngredientKnowledgebase();
		this.allIngredientsMap = this.generateAllIngredientMap();
		this.setUpRecipe();
		kitchen.addAllIngredients(this.allIngredientsMap.values());
		
		// Code for alternate planner that mimics a user taking actions and the agent reacting
		// accrodingly. More information at the bottom of the file.
		this.debug = false;
		this.allParams = this.setupParams();
		this.doneActions = new HashSet<String>();
		this.testSettingUp();
		testStephensAbilityAtUsingThisCode();
		//this.testSettingUp();
		//this.plan();
		
		/**
		 * This very simple domain assumes actions based solely on what the robot has in front of it.
		 * Seeing an empty bowl in front assumes that someone (a human, off camera) has poured the
		 * bowl into a (correct) bowl and has also moved the bowl to the robot's side. Based on this 
		 * assumption, the agent updates its state so as to plan the next action.
		 * 
		 * The knowledge in this domain lies in takePresumedAction method, which determines which actions
		 * the user taken given what the robot has infront of it. This knowledge has been put here because
		 * of the quick developemnt nature of this domain, but once refined, should be moved elsewhere --
		 * most likely formalized into PFs that will be far more extensible.
		 * 
		 * This whole domain has assumed two dry ingredients (cocoa, flour), two wet ingredients (eggs,
		 * butter) two bowls (one for dries, one for wets) and two tools. Each of these tools is used
		 * to mix the ingredients, and are supposed to prove how the agent is using affordances to
		 * determine which tool to had off : whisks are used only to mix dry ingredients, whereas
		 * spatulas are used to mix wet ingredients as well as incorporating wet ingredients into 
		 * drys (or vice versa). Of course, these could be subtituted for any pair of substantially
		 * different tools (fork and wooden spoon) to showcase the agent picking/handing off the correct
		 * tool.
		 * 
		 * Ultimately, this code should be incorporated with a publisher and subscriber that I've
		 * put somewhere on the H2R repo (this repo should be made into a jar and put into the libs for
		 * the pubsub). The idea being that every time we see a new object in the counter in front
		 * the robot, the pubsub calls the takePresumedActions() method with the name of this new object.
		 * This will cause the domain's state to change to represent the state of the world, and then
		 * the pubsub can call getRobotAction() to make the agent plan and get the new action. This
		 * action should then, by the pubsub, be translated into a message that would then be sent
		 * to baxter so it can pickup/place the correct object (be it a tool to the human, or a dirty
		 * bowl/tool into the "dirty area" of the counter). 
		 */
		//this.getNewCleanState();
		////this.testPresumptions();
	}
	
	public void testStephensAbilityAtUsingThisCode() {
	
		this.takePresumedActions("cocoa_bowl");
		GroundedAction ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		this.takePresumedActions("flour_bowl");
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		
		this.takePresumedActions("eggs_bowl");
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		
		this.takePresumedActions("butter_bowl");
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
		ga = this.getRobotAction();
		this.state = ga.executeIn(this.state);
		System.out.println(this.getGAString(ga));
		
	}
	
	public String getGAString(GroundedAction ga) {
		String str = ga.actionName();
		for (String param : ga.params) {
			str += ", " + param;
		}
		return str;
	}
	
	public State getCurrentState() {
		return this.state;
	}
	
	// Makes a new state with the correct agent and regions
	private void getNewCleanState() {
		this.state = new State();
		this.addAgents();
		this.setUpRegions();
	}
	
	// adds an object (tool or container) to the state
	public void addObject(String name, double x, double y, double z) {
		if (this.tools.contains(name)) {
			this.addTool(name, x, y ,z);
		} else if (this.bakingDishes.contains(name)) {
			this.addBakingContainer(name, x, y, z);
		} else if (this.mixingBowls.contains(name)) {
			this.addMixingContainer(name, x, y, z);
		} else {
			this.addIngredientContainer(name, x, y, z);
		}
	}
	
	public void disposeObject(State state, String objectName) {
		ObjectInstance object = state.getObject(objectName);
		ContainerFactory.changeContainerSpace(object, SpaceFactory.SPACE_DIRTY);
	}
	
	
	private void addTool(String name, double x, double y, double z) {
		String type = ToolFactory.determineType(name);
		String space = determineSpace(x);
		ObjectInstance tool = ToolFactory.getNewObjectInstance(this.domain, 
				name, type, space, x, y, z);
		this.state.addObject(tool);
	}
	private void addMixingContainer(String name, double x, double y, double z) {
		String space = determineSpace(x);
		ObjectInstance container = ContainerFactory.getNewMixingContainerObjectInstance(
				this.domain, name, new ArrayList<String>(), space, x, y, z);
		this.state.addObject(container);
	}
	
	private void addBakingContainer(String name, double x, double y, double z){
		String space = determineSpace(x);
		ObjectInstance container = ContainerFactory.getNewBakingContainerObjectInstance(
				this.domain, name, new ArrayList<String>(), space, x, y, z);
		this.state.addObject(container);
	}
	
	private void addIngredientContainer(String containerName, double x, double y, double z) {
		String space = determineSpace(x);
		String ingredientName = containerName.substring(0, containerName.length()-5);
		// butter_bowl => butter
		ObjectInstance container = ContainerFactory.getNewIngredientContainerObjectInstance(
				this.domain, containerName, ingredientName, space, x, y, z);
		ContainerFactory.setUsed(container);
		ObjectInstance ing = this.allIngredientsMap.get(ingredientName);
		IngredientFactory.changeIngredientContainer(ing, containerName);
		this.state.addObject(ing);
		this.state.addObject(container);
	}
	
	// these methods take actions. All of these could probably be subsumed by one method.
	public void takePourAction(String[] params) {
		this.state = ((PourAction)this.pour).performAction(this.state, params);
		System.out.println("Pour: " + Arrays.toString(params));
	}
	
	public void takeMoveAction(String[] params) {
		this.state = ((MoveAction)this.move).performAction(this.state, params);
		System.out.println("Move: " + Arrays.toString(params));
	}
	
	public void takeMixAction(String[] params) {
		this.state = ((MixAction)this.mix).performAction(this.state, params);
		System.out.println("Mix: "+ Arrays.toString(params));
	}
	
	public void takeHandAction(String[] params) {
		this.state = ((HandAction)this.hand).performAction(this.state, params);
		System.out.println("Move: " + Arrays.toString(params));
	}
	
	// executes the action in the state. Used for the pubsub to execute the action it percieved/
	// the action when baxter has succeeded at perfoming the action (such as moving a contianer around).
	public void executeAction(GroundedAction ga) {
		this.state = ga.executeIn(this.state);
	}
	
	
	// Given the x location, determines in what region the object is at.
	private String determineSpace(double x) {
		if (x >= robotLeft && x <= robotRight) {
			return SpaceFactory.SPACE_ROBOT;
		} else if (x >= humanLeft && x <= humanRight){
			return SpaceFactory.SPACE_HUMAN;
		} else {
			return SpaceFactory.SPACE_DIRTY;
		}
	}
	
	private AbstractMap<String, ObjectInstance> generateAllIngredientMap() {
		AbstractMap<String, ObjectInstance> map = new HashMap<String, ObjectInstance>();
		List<ObjectInstance> objs = this.knowledgebase.getPotentialIngredientObjectInstanceList(
				this.state, this.domain, this.recipe.topLevelIngredient);
		for (ObjectInstance obj : objs) {
			map.put(obj.getName(), obj);
		}
		return map;
	}
	
	private void setUpRecipe() {
		this.recipe.addIngredientSubgoals();
		this.recipe.addRequiredRecipeAttributes();
	}
	
	// A simple reward function that gives a higher reward for handing off tools rather than 
	// cleaning up.
	private RewardFunction generateRewardFunction() {
		RewardFunction rf = new RewardFunction() {
			@Override
			// Uniform cost function for an optimistic algorithm that guarantees convergence.
			public double reward(State state, GroundedAction a, State sprime) {
				String actionName = a.actionName();
				if (actionName.equals(HandAction.className)) {
					return 10;
				}
				return 1;
			}
		};
		return rf;
	}
	
	// add agents to state
	private void addAgents() {
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, AgentFactory.agentHuman));
		state.addObject(AgentFactory.getNewRobotAgentObjectInstance(domain, AgentFactory.agentRobot));
	}
	
	// adds regions to state
	private void setUpRegions() {
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_HUMAN, 
				new ArrayList<String>(), AgentFactory.agentHuman, humanTop, humanBottom, humanLeft, humanRight));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_ROBOT, 
				new ArrayList<String>(), AgentFactory.agentRobot, robotTop, robotBottom, robotLeft, robotRight));
		state.addObject(SpaceFactory.getNewDirtySpaceObjectInstance(domain, SpaceFactory.SPACE_DIRTY, 
				new ArrayList<String>(), AgentFactory.agentHuman, dirtyTop, dirtyBottom, dirtyLeft, dirtyRight));
	}
	
	// mimics an initialization message that would, presumably, give us an idea of what's on the table.
	private void testSettingUp() {
		
		this.addObject(ContainerFactory.DRY_BOWL, 75, 75, 75);
		this.addObject(ContainerFactory.WET_BOWL, 75, 75, 75);
		
		//add two tools
		this.addObject(ToolFactory.WHISK, 25, 25, 25);
		this.addObject(ToolFactory.SPATULA, 25, 25, 25);
		
		//add baking dish
		this.addObject("baking_dish", 75, 75, 75);
		
		//add ingredients (containers)
		this.addObject("flour_bowl", 75, 75, 75);
		this.addObject("cocoa_bowl", 75, 75, 75);
		
		this.addObject("eggs_bowl", 75, 75, 75);
		this.addObject("butter_bowl", 75, 75, 75);
	}

	// gets the next action the robot should take
	private GroundedAction getRobotAction() {
		Policy p = this.generatePolicy();
		GroundedAction ga = ((GroundedAction)p.getAction(this.state));
		return ga;
		
	}
	
	public String[] getRobotActionParams() {
		GroundedAction ga = this.getRobotAction();
		String[] params = new String[ga.params.length+1];
		params[0] = ga.actionName();
		for (int i = 0; i < ga.params.length; i++) {
			params[i+1] = ga.params[i];
		}
		return params;
	}
	
	// generates a polic to use for planning
	private Policy generatePolicy() {
		AffordanceCreator affCreator = new AffordanceCreator(this.domain, this.state, this.recipe.topLevelIngredient);
		AffordancesController controller = affCreator.getAffController();
		BellmanAffordanceRTDP planner = new BellmanAffordanceRTDP(this.domain, 
				this.rf ,this.tf, 0.99, new NameDependentStateHashFactory(), 0, 20, 0.05, 20, controller);

		Policy p = new AffordanceGreedyQPolicy(controller, (QComputablePlanner)planner);
		return p;
	}
	
	// returns whether object is in the robot's work area
	public boolean objectInRobotSpace(String name) {
		ObjectInstance obj = this.state.getObject(name);
		String space = this.determineSpace(obj.getRealValForAttribute("attX"));
		return space.equals(SpaceFactory.SPACE_ROBOT);
		
	}
	/**
	 * Given the name of the new object that has appeared infront of the robot, determine what actions
	 * the human must've take that would've caused the container to appear in front of the robot. Once
	 * said actions have been determined, then update the state such that the robot can later plan on 
	 * this new state that accurately represents the state of the world.
	 */
	public void takePresumedActions(String name) {
		System.out.println("Assumed Actions:");
		ObjectInstance object = state.getObject(name);
		String className = object.getObjectClass().name;
		if (className.equals(ContainerFactory.ClassName)) {
			if (ContainerFactory.isWetIngBowl(name)) {
				this.takePourAction(new String[] {"human", name, ContainerFactory.WET_BOWL});
				this.takeMoveAction(new String[] {"human", name, SpaceFactory.SPACE_ROBOT});
			} else if (ContainerFactory.isDryIngBowl(name)) {
				this.takePourAction(new String[] {"human", name, ContainerFactory.DRY_BOWL});
				this.takeMoveAction(new String[] {"human", name, SpaceFactory.SPACE_ROBOT});
			} else if (ContainerFactory.isMixingContainer(object)) {
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
			} 
		}
		if (className.equals(ToolFactory.ClassName)) {
			if (name.equals(ToolFactory.WHISK)) {
				this.takeMixAction(new String[] {"human", ContainerFactory.DRY_BOWL});
				this.takeHandAction(new String[] {"human", ToolFactory.WHISK, SpaceFactory.SPACE_ROBOT});
			} else {
				// if we're seeing a spatula, we've either mixed the wetBowl, or combined
				// the wets and dries (in either a wet or dry bowl). So, see which of the bowls
				// is in the human space and mix that one
				ObjectInstance wetBowl = state.getObject(ContainerFactory.WET_BOWL);
				String wetSpace = ContainerFactory.getSpaceName(wetBowl);
				if (wetSpace.equals(SpaceFactory.SPACE_HUMAN)) {
					this.takeMixAction(new String[] {"human", ContainerFactory.WET_BOWL});

				} else {
					this.takeMixAction(new String[] {"human", ContainerFactory.DRY_BOWL});

				}
				this.takeHandAction(new String[] {"human", ToolFactory.SPATULA, SpaceFactory.SPACE_ROBOT});
			}
			
		}
	}
	
	// simple test to see if presumptions work
	public void testPresumptions() {
		String[] steps = new String[9];
		steps[0] = "flour_bowl";
		steps[1] = "butter_bowl";
		steps[2] = "eggs_bowl";
		steps[3] = "spatula";
		steps[4] = "cocoa_bowl";
		steps[5] = "whisk";
		steps[6] = "dry_bowl";
		steps[7] = "spatula";
		steps[8] = "wet_bowl";
		this.testSettingUp();
		for (int i = 0 ; i < steps.length; i++) {
			System.out.println("\nSees: " + steps[i]);
			this.takePresumedActions(steps[i]);
			GroundedAction ga = this.getRobotAction();
			System.out.println("Robot response is move: " + Arrays.toString(ga.params));
			this.state = ga.executeIn(this.state);
		}
		while (!SpaceFactory.getContents(state.getObject(SpaceFactory.SPACE_ROBOT)).isEmpty()) {
			GroundedAction ga = this.getRobotAction();
			System.out.println("Robot response is move: " + Arrays.toString(ga.params));
			this.state = ga.executeIn(this.state);
		}
	}
	
	public static void main(String[] args) throws IOException {
		KitchenDomain kitchenDomain = new KitchenDomain();
	}
	
	/*
	 * The functions below help to test planning in this simple domain by ramdon orders of actions
	 * that could be taken by the user in order to achieve the recipe, and the planner would plan
	 * accoridngly. This is meant to emulate object recognition/action recognition on a human
	 * working alongside a robot and have them perform actions to help the human.
	 */
	public void plan() {
		boolean added = false;
		do {
		String action = this.getRandomParam();
		String[] params = this.allParams.get(action);
		this.allParams.remove(action);
		this.doneActions.add(action);
		added = this.addParams(action);
		
		if (params.length == 2) {
			this.takeMixAction(params);
			if (params[1].equals(ContainerFactory.DRY_BOWL)) {
				String whiskSpace = ToolFactory.getSpaceName(this.state.getObject(ToolFactory.WHISK));
				if (whiskSpace.equals(SpaceFactory.SPACE_HUMAN)) {
					this.takeHandAction(new String[] {"human", ToolFactory.WHISK, SpaceFactory.SPACE_ROBOT});
				} else {
					this.takeHandAction(new String[] {"human", ToolFactory.SPATULA, SpaceFactory.SPACE_ROBOT});
				}
			} else {
				this.takeHandAction(new String[] {"human", ToolFactory.SPATULA, SpaceFactory.SPACE_ROBOT});
			}
		} else {
			this.takePourAction(params);
			this.moveDirty(params);
		}
		GroundedAction ga = this.getRobotAction();
		System.out.println(Arrays.toString(ga.params));
		this.executeAction(ga);
		if (this.debug) {
			System.out.println(this.state.toString());
			System.out.println(this.allParams.keySet());
			System.out.println(added);
			System.out.println("--------------------------------\n");
		}
		} while (!this.allParams.isEmpty() || added);
	}
	
	private boolean addParams(String prevAction) { 
		if (!this.allParams.containsKey("pourCocoa") && !this.allParams.containsKey("pourFlour")) {
			if (!this.doneActions.contains("mixDry")) {
				this.allParams.put("mixDry", new String[] {"human", "dry_bowl"});
				return true;
			}
		}
		
		if (!this.allParams.containsKey("pourEggs") && !this.allParams.containsKey("pourButter")) {
			if (!this.doneActions.contains("mixWet")) {
				this.allParams.put("mixWet", new String[] {"human", "wet_bowl"});
				return true;
			}
		}
		
		if (this.allParams.isEmpty() && (prevAction.equals("mixWet") || prevAction.equals("mixDry"))) {
			boolean added = false;
			if (!this.doneActions.contains("pourWet")) {
				this.allParams.put("pourWet", new String[] {"human", "wet_bowl", "dry_bowl"});
				added = true;
			}
			if (!this.doneActions.contains("pourDry")) {
				this.allParams.put("pourDry", new String[] {"human", "dry_bowl", "wet_bowl"});
				added = true;
			}
			if (added){
				return true;
			}
		}
		
		if (this.allParams.size() == 1 &&  this.allParams.containsKey("pourDry")) {
			if (!this.doneActions.contains("mixBrowniesDryBowl")) {
				this.allParams.put("mixBrowniesDryBowl", new String[] {"human", "dry_bowl"});
				this.allParams.remove("pourDry");
				this.doneActions.add("pourDry");
				return true;
			}
		}
		
		if (this.allParams.size() == 1 && this.allParams.containsKey("pourWet")) {
			if (!this.doneActions.contains("mixBrowniesWetBowl")) {
				this.allParams.put("mixBrowniesWetBowl", new String[] {"human", "wet_bowl"});
				this.allParams.remove("pourWet");
				this.doneActions.add("pourWet");
				return true;
			}
		}
		
		if (this.allParams.isEmpty() && prevAction.equals("mixBrowniesDryBowl")) {
			if (!this.doneActions.contains("pourBrownies")) {
				this.allParams.put("pourBrownies", new String[] {"human", "dry_bowl", "baking_dish"});
				return true;
			}
		}
		
		if (this.allParams.isEmpty() && prevAction.equals("mixBrowniesWetBowl")) {
			if (!this.doneActions.contains("pourBrownies")) {
				this.allParams.put("pourBrownies", new String[] {"human", "wet_bowl", "baking_dish"});
				return true;
			}
		}
		return false;
	}
	
	private void moveDirty(String[] prevParams) {
		if (prevParams.length == 3) {
			String agent = "human";
			String bowl = prevParams[1];
			String space = SpaceFactory.SPACE_ROBOT;
			this.takeMoveAction(new String[] {agent, bowl, space});
		}
	}
	
	private String getRandomParam() {
		int rand = new Random().nextInt(this.allParams.size());
		List<String> params = new ArrayList<String> (this.allParams.keySet());
		return params.get(rand);
		
	}
	
	private AbstractMap<String, String[]> setupParams() {
		AbstractMap<String, String[]> params = new HashMap<String, String[]>();
		
		params.put("pourCocoa", new String[] {"human", "cocoa_bowl", "dry_bowl"});
		params.put("pourFlour", new String[] {"human", "flour_bowl", "dry_bowl"});
		
		params.put("pourEggs", new String[] {"human", "eggs_bowl", "wet_bowl"});
		params.put("pourButter", new String[] {"human", "butter_bowl", "wet_bowl"});
		
		return params;
	}
	
	
	public void setDebug(boolean bool) {
		this.debug = bool;
	}
}