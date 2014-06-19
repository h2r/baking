package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class PeelAction extends BakingAction {
	public PeelAction(Domain domain) {
		super("peel", domain, new String[] {AgentFactory.ClassName, IngredientFactory.ClassNameSimple});
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
		ObjectInstance spaceInstance = state.getObject(params[1]);
		this.peel(spaceInstance);
		return state;
	}
	
	protected void peel(ObjectInstance objectInstance) {
		IngredientFactory.setPeeled(objectInstance, true);
	}
}
