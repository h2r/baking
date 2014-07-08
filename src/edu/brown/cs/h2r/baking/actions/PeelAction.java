package edu.brown.cs.h2r.baking.actions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class PeelAction extends BakingAction {
	public static final String className = "peel";
	public PeelAction(Domain domain, IngredientRecipe ingredient) {
		super(PeelAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
	}

	@Override
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		
		ObjectInstance container = state.getObject(params[1]);
		
		if (ContainerFactory.isEmptyContainer(container)) {
			return false;
		}
		
		if (ContainerFactory.getContentNames(container).size() != 1) {
			return false;
		}
		
		Set<String> contents = ContainerFactory.getContentNames(container);
		for (String ingredient : contents) {
			ObjectInstance ingredientObject = state.getObject(ingredient);
			if (IngredientFactory.isPeeledIngredient(ingredientObject)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		
		ObjectInstance container = state.getObject(params[1]);
		Set<String> contents = ContainerFactory.getContentNames(container);
		for (String ingredient : contents) {
			ObjectInstance ingredientObject = state.getObject(ingredient);
			this.peel(ingredientObject);
		}
		
		return state;
	}
	
	private void peel(ObjectInstance objectInstance) {
		IngredientFactory.setPeeled(objectInstance, true);
	}
}
