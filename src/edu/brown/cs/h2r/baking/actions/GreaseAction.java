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
	public ApplicableInStateResult checkActionIsApplicableInState(State state, String[] params) {
		ApplicableInStateResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsApplicable()) {
			return superResult;
		}
		String containerName = params[1];
		ObjectInstance container = state.getObject(params[1]);
		ObjectInstance grease = state.getObject(params[2]);
		
		if (!ContainerFactory.isEmptyContainer(container)) {
			return ApplicableInStateResult.False(containerName + " is not an empty container");
		}
		
		if (!ContainerFactory.isBakingContainer(container)) {
			return ApplicableInStateResult.False(containerName + " is not an baking container");
		}
		
		if (ContainerFactory.isGreasedContainer(container)) {
			return ApplicableInStateResult.False(containerName + " is not an greased container");
		}
		
		if (!IngredientFactory.isLubricant(grease)) {
			return ApplicableInStateResult.False(containerName + " is not an greased container");
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
