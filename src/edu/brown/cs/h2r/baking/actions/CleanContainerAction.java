package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class CleanContainerAction extends BakingAction {
	public static final String className = "clean";
	public CleanContainerAction(Domain domain) {
		super(CleanContainerAction.className, domain, new String[] 
				{AgentFactory.ClassName, ContainerFactory.ClassName});
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		return superResult;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance containerInstance = state.getObject(params[1]);
		ContainerFactory.cleanContainer(containerInstance);
		return state;
	}
}
