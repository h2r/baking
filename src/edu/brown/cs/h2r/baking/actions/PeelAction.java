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

public class PeelAction extends BakingAction {
	public static final String className = "peel";
	public PeelAction(Domain domain, IngredientRecipe ingredient) {
		super(PeelAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
		String agentName = params[0];
		ObjectInstance agent =  state.getObject(agentName);
		
		String containerName = params[1];
		ObjectInstance container = state.getObject(containerName);
		
		if (ContainerFactory.isEmptyContainer(container)) {
			return BakingActionResult.failure(containerName + " is empty");
		}
		
		Set<String> contents = ContainerFactory.getContentNames(container);
		for (String ingredient : contents) {
			ObjectInstance ingredientObject = state.getObject(ingredient);
			if (IngredientFactory.isPeeledIngredient(ingredientObject)) {
				return BakingActionResult.failure(ingredient + " is already peeled");
			}
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
		
		ObjectInstance container = state.getObject(params[1]);
		Set<String> contents = ContainerFactory.getContentNames(container);
		for (String ingredient : contents) {
			ObjectInstance ingredientObject = state.getObject(ingredient);
			this.peel(ingredientObject);
		}
		
		return state;
	}
	
	public void peel(ObjectInstance objectInstance) {
		IngredientFactory.setPeeled(objectInstance, true);
	}
}
