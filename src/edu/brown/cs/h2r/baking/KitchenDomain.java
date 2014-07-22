package edu.brown.cs.h2r.baking;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import edu.brown.cs.h2r.baking.Experiments.HackathonKitchen;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
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
	
	private static double robotTop = 100;
	private static double robotBottom = 0;
	private static double robotLeft = 0;
	private static double robotRight = 50;
	
	private static double humanTop = 100;
	private static double humanBottom = 0;
	private static double humanLeft = 50;
	private static double humanRight = 100;
	
	public KitchenDomain() {
		this.kitchen = new HackathonKitchen();
		this.domain = kitchen.generateDomain();
		this.recipe = new Brownies();
		this.state = new State();
		
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		
		
		this.addAgents();
		this.setUpRegions();
		knowledgebase = new IngredientKnowledgebase();
		this.allIngredientsMap = this.generateAllIngredientMap();
		
		this.TestSettingUp();
		this.plan();
	}
	
	public void addMixingContainer(String name, double x, double y, double z) {
		String space = determineSpace(x);
		ObjectInstance container = ContainerFactory.getNewMixingContainerObjectInstance(
				this.domain, name, new ArrayList<String>(), space, x, y, z);
		this.state.addObject(container);
	}
	
	public void addBakingContainer(String name, double x, double y, double z){
		String space = determineSpace(x);
		ObjectInstance container = ContainerFactory.getNewBakingContainerObjectInstance(
				this.domain, name, new ArrayList<String>(), space, x, y, z);
		this.state.addObject(container);
	}
	
	public void addIngredientContainer(String containerName, double x, double y, double z) {
		String space = determineSpace(x);
		String ingredientName = containerName.substring(0, containerName.length()-5);
		// butter_bowl => butter
		ObjectInstance container = ContainerFactory.getNewIngredientContainerObjectInstance(
				this.domain, containerName, ingredientName, space, x, y, z);
		// should the ingredient be added to the state here?
		ObjectInstance ing = this.allIngredientsMap.get(ingredientName);
		IngredientFactory.changeIngredientContainer(ing, containerName);
		this.state.addObject(ing);
		this.state.addObject(container);
	}
	
	public void plan() {
		kitchen.addAllIngredients(this.allIngredientsMap.values());
		kitchen.PlanHackathon(this.domain, this.state, this.recipe);
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
	
	private void addAgents() {
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, AgentFactory.agentHuman));
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, AgentFactory.agentHuman));
	}
	
	private void setUpRegions() {
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_HUMAN, 
				new ArrayList<String>(), AgentFactory.agentHuman, humanTop, humanBottom, humanLeft, humanRight));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_ROBOT, 
				new ArrayList<String>(), AgentFactory.agentRobot, robotTop, robotBottom, robotLeft, robotRight));
	}
	
	private void TestSettingUp() {
		//add two bowls
		addMixingContainer("Mixing_Bowl_1", 75, 75, 75);
		addMixingContainer("Mixing_Bowl_2", 75, 75, 75);
		
		//add baking dish
		addBakingContainer("Baking_Dish", 75, 75, 75);
		
		//add ingredients (containers)
		addIngredientContainer("flour_bowl", 75, 75, 75);
		addIngredientContainer("baking_powder_bowl", 75, 75, 75);
		addIngredientContainer("salt_bowl", 75, 75, 75);
		addIngredientContainer("sea_salt_bowl", 75, 75, 75);
		addIngredientContainer("cocoa_bowl", 75, 75, 75);
		
		addIngredientContainer("eggs_bowl", 75, 75, 75);
		addIngredientContainer("butter_bowl", 75, 75, 75);
		addIngredientContainer("white_sugar_bowl", 75, 75, 75);
		addIngredientContainer("brown_sugar_bowl", 75, 75, 75);
		addIngredientContainer("vanilla_bowl", 75, 75, 75);
	}
	
	public static void main(String[] args) throws IOException {
		KitchenDomain domain = new KitchenDomain();
	}
	
}