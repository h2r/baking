package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class PeelAction extends BakingAction {
	public PeelAction(Domain domain, IngredientRecipe ingredient) {
		super("peel", domain, ingredient, new String[] {AgentFactory.ClassName, IngredientFactory.ClassNameSimple});
	}

	@Override
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		
		ObjectInstance agent =  state.getObject(params[0]);
		
		ObjectInstance ingredientInstance = state.getObject(params[1]);
		
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		
		ObjectInstance container = state.getObject(params[1]);
		Set<String> contents = ContainerFactory.getContentNames(container);
		for (String ingredient : contents) {
			ObjectInstance ingredientObject = IngredientFactory.isPeelable
		}
		this.peel(ingredientContainer);
		return state;
	}
	
	protected void peel(ObjectInstance objectInstance) {
		IngredientFactory.setPeeled(objectInstance, true);
	}
}
