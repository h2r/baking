package edu.brown.cs.h2r.baking.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class PourAction extends BakingAction {
	public static final String className = "pour";
	public PourAction(Domain domain, IngredientRecipe ingredient) {
		super(PourAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, ContainerFactory.ClassName});
		this.domain = domain;
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
		String agentName = params[0];
		ObjectInstance agent =  state.getObject(agentName);
		
		if (AgentFactory.isRobot(agent)) {
			return BakingActionResult.failure(agentName + " cannot perform this action");
		}
		
		String pouringContainerName = params[1];
		ObjectInstance pouringContainer = state.getObject(params[1]);
		
		String receivingContainerName = params[2];
		ObjectInstance receivingContainer = state.getObject(params[2]);

		String pouringContainerSpaceName = ContainerFactory.getSpaceName(pouringContainer);
		String receivingContainerSpaceName = ContainerFactory.getSpaceName(receivingContainer);
		ObjectInstance receivingContainerSpace = state.getObject(receivingContainerSpaceName);
		
		if (ContainerFactory.isEmptyContainer(pouringContainer)) {
			return BakingActionResult.failure(pouringContainerName + " is empty");
		}
		
		if (!ContainerFactory.isReceivingContainer(receivingContainer)) {
			return BakingActionResult.failure(receivingContainerName + " cannot be poured into");
		}
		
		if (pouringContainerSpaceName == null || receivingContainerSpaceName == null)
		{
			throw new RuntimeException("One of the pouring containers is not in any space");
		}
		
		if (SpaceFactory.isBaking(receivingContainerSpace)) {
			return BakingActionResult.failure(receivingContainerName + " is in the " +  receivingContainerSpaceName+ " which is a baking space!");
		}
		ObjectInstance pouringContainerSpaceObject = state.getObject(pouringContainerSpaceName);
		
		String agentOfSpace = SpaceFactory.getAgent(pouringContainerSpaceObject).iterator().next();
		//if (!agentOfSpace.equalsIgnoreCase(agentName))
		//{		
		//	return BakingActionResult.failure(agentName + " cannot work in " + pouringContainerSpaceName);
		//}
		if (!SpaceFactory.isWorking(pouringContainerSpaceObject)) {
			return BakingActionResult.failure("Pouring cannot be performed in the " + pouringContainerSpaceObject);
		}
		return BakingActionResult.success();
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}

	@Override
	public String[] getUsedObjects(State state, String[] params) {
		ObjectInstance pouringContainer = state.getObject(params[1]);
		Set<String> ingredients = ContainerFactory.getContentNames(pouringContainer);
		List<String> usedObjects = new ArrayList<String>(Arrays.asList(params));
		usedObjects.addAll(ingredients);
		
		for (String ingredient : ingredients) {
			ObjectInstance obj = state.getObject(ingredient);
			usedObjects.addAll(IngredientFactory.getRecursiveContentsAndSwapped(state, obj));
		}
		
		String[] usedObjectsArray = new String[usedObjects.size()];
		return usedObjects.toArray(usedObjectsArray);
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		return pour(state, pouringContainer, receivingContainer);
	}
	
	private State pour(State state, ObjectInstance pouringContainer, ObjectInstance receivingContainer)
	{
		ObjectInstance receivingSpace = state.getObject(ContainerFactory.getSpaceName(receivingContainer));
		if (SpaceFactory.isHeating(receivingSpace)) {
			return PourAction.pourIntoHeating(state, pouringContainer, receivingContainer, receivingSpace);
		} else {
			return PourAction.pourIntoWorking(state, pouringContainer, receivingContainer);
		}
	}
	
	// If an ingredient is poured into a heating space that is turned on, then it will get the
	// appropiate attribute.
	private static State pourIntoHeating(State state, ObjectInstance pouringContainer, 
			ObjectInstance receivingContainer, ObjectInstance receivingSpace) {
		Set<String> ingredients = ContainerFactory.getContentNames(pouringContainer);
		ObjectInstance newReceivingContainer = ContainerFactory.addIngredients(receivingContainer, ingredients);
		state.replaceObject(receivingContainer, newReceivingContainer);
		
		ObjectInstance newPouringContainer = ContainerFactory.removeContents(pouringContainer);
		state.replaceObject(pouringContainer, newPouringContainer);
		
		boolean on = SpaceFactory.getOnOff(receivingSpace);
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient);
			if (on) {
				state = Knowledgebase.heatIngredient(state, receivingContainer, ingredientInstance);
			}
			ObjectInstance newInstance = IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
			state = state.replaceObject(ingredientInstance, newInstance);
		}
		return state;
	}
	
	private static State pourIntoWorking(State state, ObjectInstance pouringContainer, 
			ObjectInstance receivingContainer) {
		Set<String> ingredients = ContainerFactory.getContentNames(pouringContainer);
		ObjectInstance newReceiving = ContainerFactory.addIngredients(receivingContainer, ingredients);
		state = state.replaceObject(receivingContainer, newReceiving);
		ObjectInstance newPouring = ContainerFactory.removeContents(pouringContainer);
		state = state.replaceObject(pouringContainer, newPouring);
		
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient);
			ObjectInstance newInstance = 
					IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
			state.replaceObject(ingredientInstance, newInstance);
		}
		
		return state;
		
		
	}
	
}