package edu.brown.cs.h2r.baking.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.StateBuilder;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class PourAction extends BakingAction {
	public static final String className = "pour";
	public PourAction(Domain domain) {
		super(PourAction.className, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, ContainerFactory.ClassName});
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
		if (!ContainerFactory.isPouringContainer(pouringContainer)) {
			return BakingActionResult.failure(pouringContainerName + " is not pourable");
		}
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
		StateBuilder builder = new StateBuilder(state);
		this.addAgentToOccupiedList(state, builder, params[0]);
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		return pour(state, builder, pouringContainer, receivingContainer);
	}
	
	private State pour(State state, StateBuilder builder, ObjectInstance pouringContainer, ObjectInstance receivingContainer)
	{
		State newState = state;
		ObjectInstance receivingSpace = state.getObject(ContainerFactory.getSpaceName(receivingContainer));
		
		if (SpaceFactory.isHeating(receivingSpace)) {
			newState = PourAction.pourIntoHeating(state, pouringContainer, receivingContainer, receivingSpace);
		} else {
			newState = PourAction.pourIntoWorking(state, builder, pouringContainer, receivingContainer);
		}
		
		//if (ContainerFactory.isPouringContainer(pouringContainer) && !ContainerFactory.isReceivingContainer(pouringContainer)) {
		//	newState = PourAction.pourIngredientContainer(state, pouringContainer, receivingContainer);
		//}
		return newState;
	}
	
	// If an ingredient is poured into a heating space that is turned on, then it will get the
	// appropiate attribute.
	private static State pourIntoHeating(State state, ObjectInstance pouringContainer, 
			ObjectInstance receivingContainer, ObjectInstance receivingSpace) {
		Set<String> ingredients = ContainerFactory.getContentNames(pouringContainer);
		
		
		ObjectInstance newPouringContainer = ContainerFactory.removeContents(pouringContainer);
		state.replaceObject(pouringContainer, newPouringContainer);
		
		boolean on = SpaceFactory.getOnOff(receivingSpace);
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient);
			if (on) {
				state = Knowledgebase.heatIngredient(state, receivingContainer, ingredientInstance);
			}
			//ObjectInstance copyInstance = IngredientFactory.getNewCopyObject(ingredientInstance, state);
			ObjectInstance newInstance = IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
			state = state.replaceObject(ingredientInstance, newInstance);
			
			ObjectInstance newReceivingContainer = ContainerFactory.addIngredient(receivingContainer, newInstance.getName());
			state.replaceObject(receivingContainer, newReceivingContainer);
		}
		return state;
	}
	
	private static State pourIntoWorking(State state, StateBuilder stateBuilder, ObjectInstance pouringContainer, 
			ObjectInstance receivingContainer) {
		Set<String> ingredients = ContainerFactory.getContentNames(pouringContainer);
		ObjectInstance newReceiving = ContainerFactory.addIngredients(receivingContainer, ingredients);
		stateBuilder.replace(receivingContainer, newReceiving);
		//state = state.replaceObject(receivingContainer, newReceiving);
		ObjectInstance newPouring = ContainerFactory.removeContents(pouringContainer);
		stateBuilder.replace(pouringContainer, newPouring);
		//state = state.replaceObject(pouringContainer, newPouring);
		
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient);
			ObjectInstance newInstance = IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
			//state = state.replaceObject(ingredientInstance, newInstance);
			stateBuilder.replace(ingredientInstance, newInstance);
		}
		
		return stateBuilder.toState();
		
		
	}
	
}