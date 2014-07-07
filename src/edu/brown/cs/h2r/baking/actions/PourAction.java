package edu.brown.cs.h2r.baking.actions;

import java.util.HashSet;
import java.util.List;
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
	private List<ObjectInstance> allIngredients;
	public PourAction(Domain domain, IngredientRecipe ingredient) {
		super(PourAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, ContainerFactory.ClassName});
		this.domain = domain;
		//this.allIngredients = ings;
	}
	
	@Override
	public ApplicableInStateResult checkActionIsApplicableInState(State state, String[] params) {
		ApplicableInStateResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsApplicable()) {
			return superResult;
		}
		
		String agentName = params[0];
		ObjectInstance agent =  state.getObject(agentName);
		
		if (AgentFactory.isRobot(agent)) {
			return ApplicableInStateResult.False(agentName + " cannot perform this action");
		}
		
		String pouringContainerName = params[1];
		ObjectInstance pouringContainer = state.getObject(params[1]);
		
		String receivingContainerName = params[2];
		ObjectInstance receivingContainer = state.getObject(params[2]);

		
		//TODO: Move this elsewhere to planner
		String pouringContainerSpace = ContainerFactory.getSpaceName(pouringContainer);
		String receivingContainerSpace = ContainerFactory.getSpaceName(receivingContainer);
		
		if (ContainerFactory.isEmptyContainer(pouringContainer)) {
			return ApplicableInStateResult.False(pouringContainerName + " is empty");
		}
		
		if (!ContainerFactory.isReceivingContainer(receivingContainer)) {
			return ApplicableInStateResult.False(receivingContainerName + " cannot be poured into");
		}
		
		if (pouringContainerSpace == null || receivingContainerSpace == null)
		{
			throw new RuntimeException("One of the pouring containers is not in any space");
		}
		
		if (!pouringContainerSpace.equalsIgnoreCase(receivingContainerSpace))
		{
			return ApplicableInStateResult.False(receivingContainerName + " cannot be poured into");
		}
		ObjectInstance pouringContainerSpaceObject = state.getObject(pouringContainerSpace);
		
		String agentOfSpace = SpaceFactory.getAgent(pouringContainerSpaceObject).iterator().next();
		if (!agentOfSpace.equalsIgnoreCase(agentName))
		{		
			return ApplicableInStateResult.False(agentName + " cannot work in " + pouringContainerSpace);
		}
		if (!SpaceFactory.isWorking(pouringContainerSpaceObject)) {
			return ApplicableInStateResult.False("Pouring cannot be performed in the " + pouringContainerSpaceObject);
		}
		return ApplicableInStateResult.True();
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsApplicable();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		pour(state, pouringContainer, receivingContainer);
		return state;
	}
	
	public void pour(State state, String pouringContainer, String receivingContainer) {
		pour(state, state.getObject(pouringContainer), state.getObject(receivingContainer));
	}
	
	public void pour(State state, ObjectInstance pouringContainer, ObjectInstance receivingContainer)
	{
		Set<String> ingredients = new HashSet<String>(ContainerFactory.getContentNames(pouringContainer));
		ContainerFactory.addIngredients(receivingContainer, ingredients);
		ContainerFactory.removeContents(pouringContainer);
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient); 
			IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
		}
		
	}
	
	public void addAllIngredients(List<ObjectInstance> ings) {
		this.allIngredients = ings;
	}
	
}