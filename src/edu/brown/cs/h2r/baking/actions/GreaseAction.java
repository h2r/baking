package edu.brown.cs.h2r.baking.actions;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class GreaseAction extends BakingAction {
	public static final String className = "grease";
	public GreaseAction(Domain domain, IngredientRecipe ingredient) {
		super(GreaseAction.className, domain, ingredient, new String[] 
				{AgentFactory.ClassName, ContainerFactory.ClassName, IngredientFactory.ClassNameSimple});
	}
	
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		ObjectInstance container = state.getObject(params[1]);
		ObjectInstance grease = state.getObject(params[2]);
		
		if (!ContainerFactory.isEmptyContainer(container)) {
			return false;
		}
		
		if (!ContainerFactory.isBakingContainer(container)) {
			return false;
		}
		
		if (!ContainerFactory.isGreasedContainer(container)) {
			return false;
		}
		
		if (!IngredientFactory.isLubricant(grease)) {
			return false;
		}
		
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance container = state.getObject(params[1]);
		ObjectInstance grease = state.getObject(params[2]);
		this.grease(state, container, grease);
		return state;
	}
	
	//TODO: Add use counts with this!
	public void grease(State state, ObjectInstance container, ObjectInstance grease)
	{
		//TODO: Change the ingredient use count!
		ContainerFactory.greaseContainer(container);
	}
}
