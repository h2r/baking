package edu.brown.cs.h2r.baking.actions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
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
		
		ObjectInstance pouringContainerSpaceObject = state.getObject(pouringContainerSpaceName);
		
		String agentOfSpace = SpaceFactory.getAgent(pouringContainerSpaceObject).iterator().next();
		if (!agentOfSpace.equalsIgnoreCase(agentName))
		{		
			return BakingActionResult.failure(agentName + " cannot work in " + pouringContainerSpaceName);
		}
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
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		pour(state, pouringContainer, receivingContainer);
		return state;
	}
	
	private void pour(State state, String pouringContainer, String receivingContainer) {
		pour(state, state.getObject(pouringContainer), state.getObject(receivingContainer));
	}
	
	private void pour(State state, ObjectInstance pouringContainer, ObjectInstance receivingContainer)
	{
		if (ContainerFactory.isEmptyContainer(receivingContainer) && 
				!ContainerFactory.getUsed(receivingContainer)) {
			ContainerFactory.setUsed(receivingContainer);
		}
		boolean bake = ContainerFactory.isBakingContainer(receivingContainer);
		Set<String> ingredients = ContainerFactory.getContentNames(pouringContainer);
		ContainerFactory.addIngredients(receivingContainer, ingredients);
		ContainerFactory.removeContents(pouringContainer);
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient);
			IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
			if (bake) {
				IngredientFactory.bakeIngredient(ingredientInstance);
			}
		}
	}
}