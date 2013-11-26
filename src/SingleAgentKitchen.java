
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
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


public class SingleAgentKitchen implements DomainGenerator {
		
	public SingleAgentKitchen() {

	}
	
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		
		Action mix = new MixAction(domain);
		//Action bake = new BakeAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		return domain;
	}
	
	public void PlanRecipeOneAgent(Domain domain, Recipe recipe)
	{
		State state = new State();
		
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewObjectInstance(domain, "shelf", false, false, false, null));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers));
		
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		ObjectClass simpleIngredientClass = domain.getObjectClass(IngredientFactory.ClassNameSimple);
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);
		ObjectInstance shelfSpace = state.getObject("shelf");
		
		List<ObjectInstance> ingredientInstances = 
				IngredientFactory.getIngredientInstancesList(simpleIngredientClass, recipe.topLevelIngredient);
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances, shelfSpace.getName());
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, shelfSpace.getName());
				state.addObject(containerInstance);
			}
		}
		
		State finalState = this.PlanIngredient(domain, state, recipe.topLevelIngredient);
	}
	
	public void PlanRecipeTwoAgents(Domain domain, Recipe recipe)
	{
		State state = new State();
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		state.addObject(AgentFactory.getNewRobotAgentObjectInstance(domain, "robot"));
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewObjectInstance(domain, "shelf", false, false, false, null));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers));
		
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		ObjectClass simpleIngredientClass = domain.getObjectClass(IngredientFactory.ClassNameSimple);
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance shelfSpace = state.getObject("counter");
		
		List<ObjectInstance> ingredientInstances = 
				IngredientFactory.getIngredientInstancesList(simpleIngredientClass, recipe.topLevelIngredient);
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances, shelfSpace.getName());
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, shelfSpace.getName());
				state.addObject(containerInstance);
			}
		}
		
		State finalState = this.PlanIngredient(domain, state, recipe.topLevelIngredient);
	}
	
	public State PlanIngredient(Domain domain, State startingState, IngredientRecipe ingredient)
	{
		State currentState = new State(startingState);
		List<IngredientRecipe> contents = ingredient.getContents();
		for (IngredientRecipe subIngredient : contents) {
			if (!subIngredient.isSimple()) {
				currentState = this.PlanIngredient(domain, currentState, subIngredient);
			}
		}
		
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, ingredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, ingredient);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction(brownies);
		RewardFunction recipeRewardFunction = new RecipeRewardFunction();
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return s.somePFGroundingIsTrue(isSuccess);
			}
		};
		final int numSteps = Recipe.getNumberSteps(ingredient);
		Heuristic heuristic = new Heuristic() {
			@Override
			public double h(State state) {
				return 0;
				//List<ObjectInstance> objects = state.getObjectsOfTrueClass(Recipe.ComplexIngredient.className);
				//double max = 0;
				//for (ObjectInstance object : objects)
				//{
				//	max = Math.max(max, this.getSubIngredients(state, object));
				//}
				//return numSteps - max;
			}
			
			public int getSubIngredients(State state, ObjectInstance object)
			{
				int count = 0;
				count += IngredientFactory.isBakedIngredient(object) ? 1 : 0;
				count += IngredientFactory.isMixedIngredient(object) ? 1 : 0;
				count += IngredientFactory.isMeltedIngredient(object) ? 1 : 0; 
				
				if (IngredientFactory.isSimple(object))
				{
					return count;
				}
				Set<String> contents = IngredientFactory.getContentsForIngredient(object);
				for (String str: contents)
				{
					count += this.getSubIngredients(state, state.getObject(str));
				}
				return count;
			}
		};
		AStar aStar = new AStar(domain, recipeRewardFunction, goalCondition, hashFactory, heuristic);
		aStar.planFromState(currentState);
		Policy policy = new DDPlannerPolicy(aStar);
		EpisodeAnalysis episodeAnalysis = 
				policy.evaluateBehavior(currentState, recipeRewardFunction, recipeTerminalFunction);	
		
		for (int i =0 ; i < episodeAnalysis.actionSequence.size(); ++i) {
			GroundedAction action = episodeAnalysis.actionSequence.get(i);
			
			double reward = episodeAnalysis.rewardSequence.get(i);
			System.out.print("Cost: " + reward + " " + action.action.getName() + " ");
			for (int j = 0; j < action.params.length; ++j) {
				System.out.print(action.params[j] + " ");
			}
			System.out.print("\n");
		}
		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
		ObjectInstance namedIngredient = null;
		for (ObjectInstance obj : finalObjects)
		{
			if (Recipe.isSuccess(endState, ingredient, obj))
			{
				namedIngredient = SingleAgentKitchen.getNewNamedComplexIngredient(obj, ingredient.getName());
				SingleAgentKitchen.switchContainersIngredients(containerObjects, obj, namedIngredient);
				endState.removeObject(obj);
				endState.addObject(namedIngredient);
				return endState;
			}
		}
		return endState;
	}
	
	public static void switchContainersIngredients(List<ObjectInstance> containers, ObjectInstance oldObject, ObjectInstance newObject)
	{
		String container = IngredientFactory.getContainer(oldObject);
		IngredientFactory.changeIngredientContainer(newObject, container);
		IngredientFactory.changeIngredientContainer(oldObject, "");
	}
	
	public static ObjectInstance getNewNamedComplexIngredient(ObjectInstance unnamedIngredient, String name)
	{
		return IngredientFactory.getNewIngredientInstance(unnamedIngredient, name);
	}
	
	public static State getOneAgent(Domain domain){
		State state = new State();
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewObjectInstance(domain, "shelf", false, false, false, null));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers));
		
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		return state;
	}
	
	public static void main(String[] args) {
		SingleAgentKitchen kitchen = new SingleAgentKitchen();
		Domain domain = kitchen.generateDomain();
		
		//kitchen.PlanRecipeOneAgent(domain, new Brownies());
		kitchen.PlanRecipeOneAgent(domain, new BrowniesSubGoals());
		//kitchen.PlanRecipeTwoAgents(domain, new BrowniesSubGoals());
		
		/*
		State state = SingleAgentKitchen.getOneAgent(domain);
		
		final Recipe brownies = new Brownies();
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, brownies.topLevelIngredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, brownies.topLevelIngredient);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction(brownies);
		RewardFunction recipeRewardFunction = new RecipeRewardFunction();
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		
		StateConditionTest goalCondition = new StateConditionTest()
		{
			@Override
			public boolean satisfies(State s) {
				return s.somePFGroundingIsTrue(isSuccess);
			}
		};
		Heuristic heuristic = new Heuristic() {
			@Override
			public double h(State state) {
				return 0;
			}
		};
		AStar aStar = new AStar(domain, recipeRewardFunction, goalCondition, 
				hashFactory, heuristic);
		aStar.planFromState(state);
		Policy policy = new DDPlannerPolicy(aStar);
		
		EpisodeAnalysis episodeAnalysis = 
				policy.evaluateBehavior(state, recipeRewardFunction, recipeTerminalFunction);
		for (int i =0 ; i < episodeAnalysis.actionSequence.size(); ++i)
		{
			GroundedAction action = episodeAnalysis.actionSequence.get(i);
			
			double reward = episodeAnalysis.rewardSequence.get(i);
			System.out.print("Cost: " + reward + " " + action.action.getName() + " ");
			for (int j = 0; j < action.params.length; ++j)
			{
				System.out.print(action.params[j] + " ");
			}
			System.out.print("\n");
		}
		//System.out.println("Action Sequence\n" + 
		//		episodeAnalysis.getActionSequenceString());
		 * 
		 */
	}
}
