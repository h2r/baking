package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class ExperimentHelper {

	public ExperimentHelper() {
		// TODO Auto-generated constructor stub
	}
	
	public static void printExpisodeSequence(List<GroundedAction> fullActions,
			List<Double> fullReward) {
		for (int i =0 ; i < fullActions.size(); ++i) {
			GroundedAction action = fullActions.get(i);
			
			double reward = -999;
			if (i < fullReward.size()) {
			reward = fullReward.get(i);
			}
			System.out.print("Cost: " + reward + " " + action.action.getName() + " ");
			for (int j = 0; j < action.params.length; ++j) {
				System.out.print(action.params[j] + " ");
			}
			System.out.print("\n");
		}
	}

	public static void printResults(List<GroundedAction> actions, List<Double> rewards) {
		double reward = 0;
		for (double r : rewards) {
			reward += r;
		}
		
		int numRobotActions = 0;
		int numHumanActions = 0;
		
		for (GroundedAction ga : actions) {
			if (ga.params[0] == "robot") {
				numRobotActions++;
			}
			else {
				numHumanActions++;
			}
		}
		
		System.out.println("Reward: " + reward);
		System.out.println("Human: " + numHumanActions);
		System.out.println("Robot: " + numRobotActions);
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
	
	public static GroundedAction getRandomGroundedAction(Domain domain, State currentState, String agent) {
		List<Action> actions = domain.getActions();
		
		List<GroundedAction> gas = new ArrayList<GroundedAction>();
		for (Action action : actions) {
			List<GroundedAction> groundedActions = currentState.getAllGroundedActionsFor(action);
			gas.addAll(groundedActions);
		}
		Random r = new Random();
		
		GroundedAction ga = gas.get(r.nextInt(gas.size()));
		
		while (ga != null && ga.params[0] != agent)
		{
			ga = gas.get(r.nextInt(gas.size()));
		}
		return ga;
	}
	
	public static void checkIngredientCompleted(IngredientRecipe ingredient,
			State endState, List<ObjectInstance> finalObjects,
			List<ObjectInstance> containerObjects) {
		ObjectInstance namedIngredient;
		for (ObjectInstance obj : finalObjects)
		{
			if (Recipe.isSuccess(endState, ingredient, obj))
			{
				namedIngredient = ExperimentHelper.getNewNamedComplexIngredient(obj, ingredient.getName());
				String container = IngredientFactory.getContainer(obj);
				ExperimentHelper.switchContainersIngredients(containerObjects, obj, namedIngredient);
				
				ObjectInstance containerInstance = endState.getObject(container);
				ContainerFactory.removeContents(containerInstance);
				ContainerFactory.addIngredient(containerInstance, ingredient.getName());
				endState.removeObject(obj);
				endState.addObject(namedIngredient);
			}
		}
	}
}
