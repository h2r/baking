package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;


public class MeltAction extends BakingAction {
	public static final String className = "melt";
	public MeltAction(Domain domain, IngredientRecipe ingredient) {
		super(MeltAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		ObjectInstance containerInstance = state.getObject(params[1]);
		if (ContainerFactory.isEmptyContainer(containerInstance)) {
			return false;
		}
		for (String ingredientName : ContainerFactory.getContentNames(containerInstance)) {
			if (IngredientFactory.isMeltedIngredient(state.getObject(ingredientName)) ||
					IngredientFactory.isMeltedAtRoomTemperature(state.getObject(ingredientName))) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance container = state.getObject(params[1]);
		this.melt(state, container);
		return state;
	}
	
	public static void melt(State state, ObjectInstance container)
	{
		for (String ingredientName : ContainerFactory.getContentNames(container)) {
			ObjectInstance ingredient = state.getObject(ingredientName);
			IngredientFactory.meltIngredient(ingredient);
		}
	}
}
