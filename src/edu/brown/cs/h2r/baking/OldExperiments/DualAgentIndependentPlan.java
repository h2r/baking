package edu.brown.cs.h2r.baking.Experiments;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
//import edu.brown.cs.h2r.baking.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
/*
The issue here, appears to be that our makespan calculation can't account for subgoals. Because we plan subgoals
sequentially, a plan that optimally uses the two agents won't be computed. Instead, we need to change the task to
allow for parallel plans. This will increase the possible states to visit, and also probable won't speed up 
planning time. 

Therefore, this is interesting because if two independent planners attempt to speed up planning and use subgoals,
they will both collide if they both choose the same subgoal. Instead, you only want as much cooperation as will
speed up the total time to solve the problem.
*/
public class DualAgentIndependentPlan  implements DomainGenerator {

	public DualAgentIndependentPlan() {
	}
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(MakeSpanFactory.getObjectClass(domain));
		
		return domain;
	}
	
	public void PlanRecipeTwoAgents(Domain domain, Recipe recipe)
	{
		System.out.println("Creating two-agent initial start state");
		State state = new State();
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		//Action bake = new BakeAction(domain);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "robot"));
		state.addObject(MakeSpanFactory.getNewObjectInstance(domain, "makeSpan", 2));
		List<String> containers = Arrays.asList("mixing_bowl_1");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "shelf", null, null));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter_human", containers, "human"));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter_robot", containers, "robot"));
	
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "shelf"));
		}
		
		this.PlanIngredient(domain, state, recipe.topLevelIngredient);
	}
	
	public State PlanIngredient(Domain domain, State startingState, IngredientRecipe ingredient)
	{
		State currentState = new State(startingState);
		
		List<IngredientRecipe> contents = ingredient.getContents();
		for (IngredientRecipe subIngredient : contents) {
			if (!subIngredient.isSimple()) {
				System.out.println("Planning ingredient " + subIngredient.getName());
				currentState = this.PlanIngredient(domain, currentState, subIngredient);
			}
		}
		
		ObjectClass simpleIngredientClass = domain.getObjectClass(IngredientFactory.ClassNameSimple);
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance shelfSpace = currentState.getObject("shelf");
		
		List<ObjectInstance> ingredientInstances = 
				IngredientFactory.getSimpleIngredients(simpleIngredientClass, ingredient);
		List<ObjectInstance> containerInstances = Recipe.getContainersAndIngredients(containerClass, ingredientInstances, shelfSpace.getName());
		
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (currentState.getObject(ingredientInstance.getName()) == null) {
				currentState.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (currentState.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, shelfSpace.getName());
				currentState.addObject(containerInstance);
			}
		}
		
		
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, ingredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, ingredient);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction(brownies);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction();
		RewardFunction humanRewardFunction = new RecipeAgentSpecificMakeSpanRewardFunction("human");
		RewardFunction robotRewardFunction = new RecipeAgentSpecificMakeSpanRewardFunction("robot");
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return isSuccess.somePFGroundingIsTrue(s);
			}
		};
		//final int numSteps = Recipe.getNumberSteps(ingredient);
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
			/*
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
			}*/
		};
		boolean finished = false;
		State endState = startingState;
		List<GroundedAction> fullActions = new ArrayList<GroundedAction>();
		List<Double> fullReward = new ArrayList<Double>();
		boolean currentAgent = false;
		while (!finished) {
			currentAgent = !currentAgent;
			RewardFunction recipeRewardFunction = (currentAgent) ? humanRewardFunction : robotRewardFunction;
			AStar aStar = new AStar(domain, recipeRewardFunction, goalCondition, hashFactory, heuristic);
			aStar.planFromState(currentState);
			Policy policy = new DDPlannerPolicy(aStar);
			EpisodeAnalysis episodeAnalysis = 
					policy.evaluateBehavior(currentState, recipeRewardFunction, recipeTerminalFunction);	
			
			
			
			System.out.println("Taking action " + episodeAnalysis.actionSequence.get(0).action.getName());
			fullActions.add(episodeAnalysis.actionSequence.get(0));
			fullReward.add(episodeAnalysis.rewardSequence.get(0));
			currentState = episodeAnalysis.stateSequence.get(1);
			endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
			List<ObjectInstance> finalObjects = 
					new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
			List<ObjectInstance> containerObjects =
					new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
			ObjectInstance namedIngredient = null;
			for (ObjectInstance obj : finalObjects)
			{
				if (Recipe.isSuccess(endState, ingredient, obj))
				{
					namedIngredient = DualAgentIndependentPlan.getNewNamedComplexIngredient(obj, ingredient.getName());
					String container = IngredientFactory.getContainer(obj);
					DualAgentIndependentPlan.switchContainersIngredients(containerObjects, obj, namedIngredient);
					
					ObjectInstance containerInstance = endState.getObject(container);
					ContainerFactory.removeContents(containerInstance);
					ContainerFactory.addIngredient(containerInstance, ingredient.getName());
					endState.removeObject(obj);
					endState.addObject(namedIngredient);
					//return endState;
				}
			}
			if (episodeAnalysis.actionSequence.size() <= 1) {
				System.out.println("Action sequence size: " + episodeAnalysis.actionSequence.size());
				finished = true;
			}
			
			for (int i =0 ; i < fullActions.size(); ++i) {
				GroundedAction action = fullActions.get(i);
				
				double reward = fullReward.get(i);
				System.out.print("Cost: " + reward + " " + action.action.getName() + " ");
				for (int j = 0; j < action.params.length; ++j) {
					System.out.print(action.params[j] + " ");
				}
				System.out.print("\n");
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

	public static void main(String[] args) throws IOException {
		DualAgentIndependentPlan kitchen = new DualAgentIndependentPlan();
		System.out.println("Generating Domain");
		Domain domain = kitchen.generateDomain();
		kitchen.PlanRecipeTwoAgents(domain, new Brownies());
	}
}
