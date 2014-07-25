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
	
	public KitchenDomain() {
		this.kitchen = new HackathonKitchen();
		this.domain = kitchen.generateDomain();
		this.recipe = new Brownies();
		this.state = new State();
		final PropositionalFunction cleanBowl = new BowlsClean(
				AffordanceCreator.CLEAN_PF, this.domain, this.recipe.topLevelIngredient);
		final RecipeFinished finish = new RecipeFinished(AffordanceCreator.FINISH_PF, this.domain,
				this.recipe.topLevelIngredient);
		final RecipeBotched botched = new RecipeBotched(AffordanceCreator.FINISH_PF, this.domain,
				this.recipe.topLevelIngredient);
		 this.tf = new RecipeTerminalFunction(cleanBowl, finish, botched);
		 this.rf = this.generateRewardFunction();

		this.mix = new MixAction(domain, recipe.topLevelIngredient);
		this.pour = new PourAction(domain, recipe.topLevelIngredient);
		this.move = new MoveAction(domain, recipe.topLevelIngredient);
		this.hand = new HandAction(domain, recipe.topLevelIngredient);
		
		this.allParams = this.setupParams();
		this.doneActions = new HashSet<String>();
		
		this.addAgents();
		this.setUpRegions();
		knowledgebase = new IngredientKnowledgebase();
		this.allIngredientsMap = this.generateAllIngredientMap();
		this.setUpRecipe();
		kitchen.addAllIngredients(this.allIngredientsMap.values());
	}
	
	public State getCurrentState() {
		return this.state;
	}
	
	public void addObject(String name, double x, double y, double z) {
		if (this.tools.contains(name)) {
			this.addTool(name, x, y ,z);
		}
		if (this.bakingDishes.contains(name)) {
			this.addBakingContainer(name, x, y, z);
		} else if (this.mixingBowls.contains(name)) {
			this.addMixingContainer(name, x, y, z);
		} else {
			this.addIngredientContainer(name, x, y, z);
		}
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
		// should the ingredient be added to the state here?
		ObjectInstance ing = this.allIngredientsMap.get(ingredientName);
		IngredientFactory.changeIngredientContainer(ing, containerName);
		this.state.addObject(ing);
		this.state.addObject(container);
	}
	
	public void takePourAction(String[] params) {
		this.state = ((PourAction)this.pour).performAction(this.state, params);
		this.checkAndClearSubgoals();
	}
	
	public void takeMoveAction(String[] params) {
		this.state = ((MoveAction)this.move).performAction(this.state, params);
		this.checkAndClearSubgoals();
	}
	
	public void takeMixAction(String[] params) {
		this.state = ((MixAction)this.mix).performAction(this.state, params);
		this.checkAndClearSubgoals();
	}
	
	public void plan() {
		boolean added = false;
		do {
		String action = this.getRandomParam();
		String[] params = this.allParams.get(action);
		System.out.println(Arrays.toString(params));
		this.allParams.remove(action);
		this.doneActions.add(action);
		added = this.addParams(action);
		
		if (params.length == 2) {
			this.takeMixAction(params);
			if (params[1].equals(ContainerFactory.DRY_BOWL)) {
				String whiskSpace = ToolFactory.getSpaceName(this.state.getObject(ToolFactory.WHISK));
				if (whiskSpace.equals(SpaceFactory.SPACE_HUMAN)) {
					this.state = this.hand.performAction(this.state, new String[] 
							{"human", ToolFactory.WHISK, SpaceFactory.SPACE_ROBOT});
					System.out.println("human move whisk to robotCounter");
				} else {
					this.state = this.hand.performAction(this.state, new String[] 
							{"human", ToolFactory.SPATULA, SpaceFactory.SPACE_ROBOT});
					System.out.println("human move spatula to robotCounter");
				}
			} else {
				this.state = this.hand.performAction(this.state, new String[] 
						{"human", ToolFactory.SPATULA, SpaceFactory.SPACE_ROBOT});
				System.out.println("human move spatula to robotCounter");
			}
		} else {
			this.takePourAction(params);
			this.moveDirty(params);
		}
		this.robotTakeAction();
		} while (!this.allParams.isEmpty() || added);
	}
	
	private String determineSpace(double x) {
		if (x >= robotLeft && x <= robotRight) {
			return SpaceFactory.SPACE_ROBOT;
		}
		return SpaceFactory.SPACE_HUMAN;
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
		this.recipe.setUpSubgoals(this.domain);
		this.recipe.addIngredientSubgoals();
		this.recipe.addRequiredRecipeAttributes();
	}
	
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
	
	private void addAgents() {
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, AgentFactory.agentHuman));
		state.addObject(AgentFactory.getNewRobotAgentObjectInstance(domain, AgentFactory.agentRobot));
	}
	
	private void setUpRegions() {
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_HUMAN, 
				new ArrayList<String>(), AgentFactory.agentHuman, humanTop, humanBottom, humanLeft, humanRight));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_ROBOT, 
				new ArrayList<String>(), AgentFactory.agentRobot, robotTop, robotBottom, robotLeft, robotRight));
		state.addObject(SpaceFactory.getNewDirtySpaceObjectInstance(domain, SpaceFactory.SPACE_DIRTY, 
				new ArrayList<String>(), AgentFactory.agentHuman, dirtyTop, dirtyBottom, dirtyLeft, dirtyRight));
	}
	
	private void checkAndClearSubgoals() {
		BakingSubgoal completed = null;
		for (BakingSubgoal sg : this.recipe.getSubgoals()) {
			if (sg.goalCompleted(this.state)) {
				if (sg.allPreconditionsCompleted(this.state)) {
					completed = sg;
					break;
				}
			}
		}
		if (completed != null) {
			this.recipe.removeSubgoal(completed);
			IngredientFactory.hideUnecessaryIngredients(this.state, domain,
					completed.getIngredient(), new ArrayList<ObjectInstance>(this.allIngredientsMap.values()));
		}
	}
	public void test() {
		this.testSettingUp();
		this.testActions();
	}
	
	private void testSettingUp() {
		//add two bowls
		addMixingContainer(ContainerFactory.DRY_BOWL, 75, 75, 75);
		addMixingContainer(ContainerFactory.WET_BOWL, 75, 75, 75);
		
		//add two tools
		addTool(ToolFactory.WHISK, 25, 25, 25);
		addTool(ToolFactory.SPATULA, 25, 25, 25);
		
		//add baking dish
		addBakingContainer("baking_dish", 75, 75, 75);
		
		//add ingredients (containers)
		addIngredientContainer("flour_bowl", 75, 75, 75);
		addIngredientContainer("cocoa_bowl", 75, 75, 75);
		
		addIngredientContainer("eggs_bowl", 75, 75, 75);
		addIngredientContainer("butter_bowl", 75, 75, 75);
	}
	private AbstractMap<String, String[]> setupParams() {
		AbstractMap<String, String[]> params = new HashMap<String, String[]>();
		
		params.put("pourCocoa", new String[] {"human", "cocoa_bowl", "dry_bowl"});
		params.put("pourFlour", new String[] {"human", "flour_bowl", "dry_bowl"});
		
		params.put("pourEggs", new String[] {"human", "eggs_bowl", "wet_bowl"});
		params.put("pourButter", new String[] {"human", "butter_bowl", "wet_bowl"});
		
		return params;
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
			System.out.println(agent + " move " + bowl + " to " + space);
			this.takeMoveAction(new String[] {agent, bowl, space});
		}
	}
	
	private String getRandomParam() {
		int rand = new Random().nextInt(this.allParams.size());
		List<String> params = new ArrayList<String> (this.allParams.keySet());
		return params.get(rand);
		
	}
	
	private void robotTakeAction() {
		Policy p = this.generatePolicy();
		GroundedAction ga = ((GroundedAction)p.getAction(this.state));
		this.state = ga.executeIn(this.state);
		System.out.println(Arrays.toString(ga.params));
		
	}
	
	private Policy generatePolicy() {
		AffordanceCreator affCreator = new AffordanceCreator(this.domain, this.state, this.recipe.topLevelIngredient);
		AffordancesController controller = affCreator.getAffController();
		BellmanAffordanceRTDP planner = new BellmanAffordanceRTDP(this.domain, 
				this.rf ,this.tf, 0.99, new NameDependentStateHashFactory(), 0, 20, 0.05, 20, controller);

		Policy p = new AffordanceGreedyQPolicy(controller, (QComputablePlanner)planner);
		return p;
	}
	private void testActions() {
		//this.takePourAction(new String[] {"human", "vanilla_bowl", "mixing_bowl_1"});
		//this.takePourAction(new String[] {"human", "flour_bowl", "mixing_bowl_2"});
	}
	
	public static void main(String[] args) throws IOException {
		KitchenDomain kitchenDomain = new KitchenDomain();
	}
	
}