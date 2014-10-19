package edu.brown.cs.h2r.baking.actions;

import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class GreaseAction extends BakingAction {
	public static final String className = "grease";
	public GreaseAction(Domain domain) {
		super(GreaseAction.className, domain, new String[] 
				{AgentFactory.ClassName, ContainerFactory.ClassName, IngredientFactory.ClassNameSimple});
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		return BakingActionResult.failure("Do not use this action"); /*
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		String containerName = params[1];
		ObjectInstance container = state.getObject(params[1]);
		
		String ingredientName = params[2];
		ObjectInstance grease = state.getObject(params[2]);
		
		if (!ContainerFactory.isEmptyContainer(container)) {
			return BakingActionResult.failure(containerName + " is not an empty container");
		}
		
		if (!ContainerFactory.isBakingContainer(container)) {
			return BakingActionResult.failure(containerName + " is not an baking container");
		}
		
		if (ContainerFactory.isGreasedContainer(container)) {
			return BakingActionResult.failure(containerName + " is not an greasable container");
		}
		
		if (!IngredientFactory.isLubricant(grease)) {
			return BakingActionResult.failure(ingredientName + " is not a suitable grease");
		}
		
		return BakingActionResult.success();*/
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance container = state.getObject(params[1]);
		ObjectInstance grease = state.getObject(params[2]);
		return this.grease(state, container, grease);
	}
	
	//TODO: Add use counts with this!
	private State grease(State state, ObjectInstance container, ObjectInstance grease)
	{
		//TODO: Change the ingredient use count!
		ObjectInstance greasedContainer = ContainerFactory.greaseContainer(container);
		return state.replaceObject(container, greasedContainer);
	}
}
