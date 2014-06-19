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
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		ObjectInstance agent = state.getObject(params[0]);
		if (AgentFactory.isRobot(agent)) {
			return false;
		}
		
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);

		
		//TODO: Move this elsewhere to planner
		String pouringContainerSpace = ContainerFactory.getSpaceName(pouringContainer);
		String receivingContainerSpace = ContainerFactory.getSpaceName(receivingContainer);
		
		if (ContainerFactory.isEmptyContainer(pouringContainer)) {
			return false;
		}
		
		if (!ContainerFactory.isReceivingContainer(receivingContainer)) {
			return false;
		}
		
		if (pouringContainerSpace == null || receivingContainerSpace == null)
		{
			throw new RuntimeException("One of the pouring containers is not in any space");
		}
		
		if (pouringContainerSpace != receivingContainerSpace)
		{
			return false;
		}
		ObjectInstance pouringContainerSpaceObject = state.getObject(pouringContainerSpace);
		
		String agentOfSpace = SpaceFactory.getAgent(pouringContainerSpaceObject).iterator().next();
		if (agentOfSpace != agent.getName())
		{		
			return false;
		}
		if (!SpaceFactory.isWorking(pouringContainerSpaceObject)) {
			return false;
		}
		
		if (ContainerFactory.isMixingContainer(pouringContainer) && (ContainerFactory.isEmptyContainer(receivingContainer))) {
			return false;
		}
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		pour(state, pouringContainer, receivingContainer);
		return state;
	}
	
	protected void pour(State state, ObjectInstance pouringContainer, ObjectInstance receivingContainer)
	{
		Set<String> ingredients = new HashSet<String>(ContainerFactory.getContentNames(pouringContainer));
		ContainerFactory.addIngredients(receivingContainer, ingredients);
		//if (shouldRemove(state, pouringContainer)) {
			//ContainerFactory.removeContents(pouringContainer);
		//}
		ContainerFactory.removeContents(pouringContainer);
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient); 
			IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
		}
		
	}
	
	public void addAllIngredients(List<ObjectInstance> ings) {
		this.allIngredients = ings;
	}
	
	//TODO: Make this a PF?
	private boolean shouldRemove(State state, ObjectInstance container) {
		ObjectInstance obj = null;
		for (String name :ContainerFactory.getContentNames(container)) {
			obj = state.getObject(name);
			break;
		}
		return IngredientFactory.getUseCount(obj) == 1;
	}
}