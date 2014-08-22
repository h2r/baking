package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
import burlap.oomdp.auxiliary.DomainGenerator;
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
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Agents.Agent;
import edu.brown.cs.h2r.baking.Agents.Baxter;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.Knowledgebase.ToolKnowledgebase;
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
import edu.brown.cs.h2r.baking.actions.CleanContainerAction;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PeelAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;

// TODO need to figure out why planner is not planning
public class CleaningPassingObjectsKitchen implements DomainGenerator {

	List<ObjectInstance> allIngredients;
	List<BakingSubgoal> ingSubgoals;
	private IngredientRecipe topLevelIngredient;
	private Recipe recipe;
	private Agent agent;
	IngredientKnowledgebase knowledgebase;
	public CleaningPassingObjectsKitchen(Recipe recipe)
	{
		this.recipe = recipe;
		this.topLevelIngredient = recipe.topLevelIngredient;
		this.knowledgebase = new IngredientKnowledgebase();
	}
	
	public void setAgent(Agent agent)
	{
		this.agent = agent;
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
		domain.addObjectClass(ToolFactory.createObjectClass(domain));
		domain.setObjectIdentiferDependence(true);
		
		Action mix = new MixAction(domain, this.topLevelIngredient);
		Action pour = new PourAction(domain, this.topLevelIngredient);
		Action move = new MoveAction(domain, this.topLevelIngredient);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action peel = new PeelAction(domain, this.topLevelIngredient);
		Action clean = new CleanContainerAction(domain);

		return domain;
	}
	
	public State generateInitialState(Domain domain) {
		State state = new State();
		this.recipe.setUpSubgoals(domain);
		// creates ingredient-only subgoals 
		this.recipe.addIngredientSubgoals();
		this.recipe.addRequiredRecipeAttributes();
		
		ObjectInstance human = AgentFactory.getNewHumanAgentObjectInstance(domain, "human");
		state.addObject(human);
		
		ObjectInstance baxter = AgentFactory.getNewRobotAgentObjectInstance(domain, "baxter");
		state.addObject(baxter);
		
		List<String> containers = 
				Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish", "melting_pot");
		
		ObjectInstance counter = 
				SpaceFactory.getNewWorkingSpaceObjectInstance(
						domain, SpaceFactory.SPACE_COUNTER, containers, "human");
		state.addObject(counter);

		ObjectInstance bakingDish = 
				ContainerFactory.getNewBakingContainerObjectInstance(
						domain, "baking_dish", null, SpaceFactory.SPACE_COUNTER);
		state.addObject(bakingDish);
		
		ObjectInstance meltingPot = 
				ContainerFactory.getNewHeatingContainerObjectInstance(
						domain, "melting_pot", null, SpaceFactory.SPACE_COUNTER);
		state.addObject(meltingPot);
		
		ObjectInstance oven = 
				SpaceFactory.getNewBakingSpaceObjectInstance(domain, SpaceFactory.SPACE_OVEN, null, "");
		state.addObject(oven);
		
		ObjectInstance stove = 
				SpaceFactory.getNewHeatingSpaceObjectInstance(domain, SpaceFactory.SPACE_STOVE, null, "");
		state.addObject(stove);
		
		for (String container : containers) { 
			ObjectInstance containerObject = 
					ContainerFactory.getNewMixingContainerObjectInstance(
							domain, container, null, SpaceFactory.SPACE_COUNTER);
			state.addObject(containerObject);
		}
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = state.getObject(SpaceFactory.SPACE_COUNTER);

		List<ObjectInstance> ingredientInstances =
				this.knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, recipe.topLevelIngredient);
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances, counterSpace.getName());
		
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, counterSpace.getName());
				state.addObject(containerInstance);
			}
		}

		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (IngredientFactory.getUseCount(ingredientInstance) >= 1) {
				ObjectInstance ing = state.getObject(ingredientInstance.getName());
				IngredientFactory.changeIngredientContainer(ing, ing.getName()+"_bowl");
				ContainerFactory.addIngredient(state.getObject(ing.getName()+"_bowl"), ing.getName());
				SpaceFactory.addContainer(state.getObject(SpaceFactory.SPACE_COUNTER), state.getObject(ing.getName()+"_bowl"));
			}
		}
		
		// Get the tools!
		ToolKnowledgebase toolKnowledgebase = new ToolKnowledgebase();
		for (Entry<String, String[]> tool : toolKnowledgebase.getToolMap().entrySet()) {
			String name = tool.getKey();
			String[] toolInfo = tool.getValue();
			String toolTrait = toolInfo[0];
			String toolAttribute = toolInfo[1];
			
			ObjectInstance toolObject;
			if (toolInfo.length == 3) {
				toolObject = ToolFactory.getNewCarryingToolObjectInstance(
						domain, name, toolTrait, toolAttribute, SpaceFactory.SPACE_COUNTER);
			}
			else {
				toolObject = ToolFactory.getNewSimpleToolObjectInstance(
						domain, name, toolTrait, toolAttribute, SpaceFactory.SPACE_COUNTER);
			}
			state.addObject(toolObject);
		}
		return state;
	}
	
	public State addObservationContainerInRegion(State currentState, String container, String regionSpace)
	{
		State state = new State(currentState);
		ObjectInstance object = state.getObject(container);
		ObjectInstance space = state.getObject(regionSpace);
		if (object != null && space != null)
		{
			ContainerFactory.changeContainerSpace(object, regionSpace);
		}
		return state;
	}

	public String[] getRobotsAction(State state)
	{
		AbstractGroundedAction action = this.agent.getAction(state);
		return (action == null) ? null : action.params;
	}
	
	public static void main(String[] args)
	{
		Recipe brownies = new Brownies();
		CleaningPassingObjectsKitchen kitchen = new CleaningPassingObjectsKitchen(brownies);
		Domain domain = kitchen.generateDomain();
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		Agent baxter = new Baxter(domain, brownies, hashFactory);
		kitchen.setAgent(baxter);
		
		State state = kitchen.generateInitialState(domain);
		List<String> items = Arrays.asList("cocoa_bowl", "flour_bowl", "eggs_bowl", "sugar_bowl");
		String[] params;
		
		for (String item : items)
		{
			System.out.println("Observing " + item + " on counter");
			state = kitchen.addObservationContainerInRegion(state, item, "counter");
			params = kitchen.getRobotsAction(state);
			if (params == null) {
				System.out.println("Robot does nothing");
			}
			else {
				System.out.println("Robot takes action: " + params.toString());
			}
		}
	}
}
