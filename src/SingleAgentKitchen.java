
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
		domain.addObjectClass(AgentFactory.createObjectClass(domain));
		
		Action mix = new MixAction(domain);
		Action bake = new BakeAction(domain);
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
		
		State finalState = this.PlanIngredient(domain, state, recipe.topLevelIngredient);
	}
	
	public State PlanIngredient(Domain domain, State startingState, IngredientFactory ingredient)
	{
		State currentState = new State(startingState);
		Set<String> contents = IngredientFactory.getIngredientContents(complexIngredient)
		for (IngredientFactory subIngredient : )
		for (Recipe.IngredientFactory subIngredient : ingredient.Contents)
		{
			if (subIngredient instanceof Recipe.ComplexIngredient)
			{
				currentState = this.PlanIngredient(domain, currentState, (Recipe.ComplexIngredient)subIngredient);
			}
		}
		
		ObjectClass simpleIngredientClass = domain.getObjectClass(Recipe.SimpleIngredient.className);
		
		ObjectClass containerClass = domain.getObjectClass(ContainerClass.className);
		
		List<ObjectInstance> ingredientInstances = ingredient.getSimpleObjectInstances(simpleIngredientClass);
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances);
		
		for (ObjectInstance ingredientInstance : ingredientInstances)
		{
			if (currentState.getObject(ingredientInstance.getName()) == null)
			{
				currentState.addObject(ingredientInstance);
			}
		}
		ObjectInstance shelfSpace = currentState.getObject("shelf");
		for (ObjectInstance containerInstance : containerInstances)
		{
			if (currentState.getObject(containerInstance.getName()) == null)
			{
				containerInstance.addRelationalTarget(ATTINSPACE, shelfSpace.getName());
				currentState.addObject(containerInstance);
			}
		}
		
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, ingredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, ingredient);
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
				count += (object.getDiscValForAttribute(Recipe.Ingredient.attBaked) == 1) ? 1 : 0;
				count += (object.getDiscValForAttribute(Recipe.Ingredient.attMelted) == 1) ? 1 : 0;
				count += (object.getDiscValForAttribute(Recipe.Ingredient.attMixed) == 1) ? 1 : 0; 
				
				if (object.getObjectClass().name == Recipe.SimpleIngredient.className)
				{
					return count;
				}
				Set<String> contents = object.getAllRelationalTargets(Recipe.ComplexIngredient.attContains);
				for (String str: contents)
				{
					count += this.getSubIngredients(state, state.getObject(str));
				}
				return count;
			}
		};
		AStar aStar = new AStar(domain, recipeRewardFunction, goalCondition, 
				hashFactory, heuristic);
		aStar.planFromState(currentState);
		Policy policy = new DDPlannerPolicy(aStar);
		
		EpisodeAnalysis episodeAnalysis = 
				policy.evaluateBehavior(currentState, recipeRewardFunction, recipeTerminalFunction);	
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
		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(Recipe.ComplexIngredient.className));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerClass.className));
		ObjectInstance namedIngredient = null;
		for (ObjectInstance obj : finalObjects)
		{
			if (Recipe.isSuccess(endState, ingredient, obj))
			{
				namedIngredient = SingleAgentKitchen.getNewNamedComplexIngredient(obj, ingredient.Name);
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
		for (ObjectInstance container : containers)
		{
			if (container.getAllRelationalTargets(ContainerClass.ATTCONTAINS).contains(oldObject.getName()))
			{
				container.getAllRelationalTargets(ContainerClass.ATTCONTAINS).remove(oldObject.getName());
				container.addRelationalTarget(ContainerClass.ATTCONTAINS, newObject.getName());
			}
		}
	}
	
	public static ObjectInstance getNewNamedComplexIngredient(ObjectInstance unnamedIngredient, String name)
	{
		ObjectInstance namedIngredient = new ObjectInstance(unnamedIngredient.getObjectClass(), name);
		int baked = unnamedIngredient.getDiscValForAttribute(Recipe.Ingredient.attBaked);
		namedIngredient.setValue(Recipe.Ingredient.attBaked, baked);
		int mixed = unnamedIngredient.getDiscValForAttribute(Recipe.Ingredient.attMixed);
		namedIngredient.setValue(Recipe.Ingredient.attMixed, mixed);
		int melted = unnamedIngredient.getDiscValForAttribute(Recipe.Ingredient.attMelted);
		namedIngredient.setValue(Recipe.Ingredient.attMelted, melted);
		
		Set<String> contents = unnamedIngredient.getAllRelationalTargets(Recipe.ComplexIngredient.attContains);
		for (String subIngredient : contents)
		{
			namedIngredient.addRelationalTarget(Recipe.ComplexIngredient.attContains, subIngredient);
		}
		
		return namedIngredient;		
	}
	
	public static State getOneAgent(Domain domain){
		State state = new State();
		
		ObjectInstance human = new ObjectInstance(domain.getObjectClass(CLASSAGENT), "human");
		human.setValue(ATTROBOT, 0);
		state.addObject(human);
		
		ObjectInstance robot = new ObjectInstance(domain.getObjectClass(CLASSAGENT), "robot");
		robot.setValue(ATTROBOT, 1);
		state.addObject(robot);
		
		ObjectInstance shelfSpace = new ObjectInstance(domain.getObjectClass(CLASSSPACE), "shelf");
		shelfSpace.setValue(ATTMIXINGSPACE, 0);
		state.addObject(shelfSpace);
		ObjectInstance counterSpace = new ObjectInstance(domain.getObjectClass(CLASSSPACE), "counter");
		counterSpace.setValue(ATTMIXINGSPACE, 1);
		state.addObject(counterSpace);
		
		ObjectInstance mixingBowl = 
				new ObjectInstance(
						domain.getObjectClass(ContainerClass.className), 
						"mixing_bowl_1");
		mixingBowl.setValue(ContainerClass.ATTRECEIVING, 1);
		mixingBowl.setValue(ContainerClass.ATTHEATING, 0);
		mixingBowl.setValue(ContainerClass.ATTMIXING, 1);
		
		state.addObject(mixingBowl);
		ObjectClass simpleIngredientClass = domain.getObjectClass(Recipe.SimpleIngredient.className);
		
		Recipe brownies = new Brownies();
		ObjectClass containerClass = domain.getObjectClass(ContainerClass.className);
		
		List<ObjectInstance> ingredientInstances = brownies.getRecipeList(simpleIngredientClass);
		List<ObjectInstance> containerInstances = Brownies.getContainers(containerClass, ingredientInstances);
		
		for (ObjectInstance ingredientInstance : ingredientInstances)
		{
			state.addObject(ingredientInstance);
		}
		for (ObjectInstance containerInstance : containerInstances)
		{
			containerInstance.addRelationalTarget(ATTINSPACE, shelfSpace.getName());
			state.addObject(containerInstance);
			
		}
		return state;
	}
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		SingleAgentKitchen kitchen = new SingleAgentKitchen();
		Domain domain = kitchen.generateDomain();
		
		//kitchen.PlanRecipeOneAgent(domain, new Brownies());
		//kitchen.PlanRecipeOneAgent(domain, new BrowniesSubGoals());
		kitchen.PlanRecipe(domain, new BrowniesSubGoals());
		
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
