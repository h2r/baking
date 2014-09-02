package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;

public class WaitAction extends BakingAction {
	public static final String ClassName = "wait";
	public WaitAction(Domain domain) {
		super(ClassName, domain, new String[] {AgentFactory.ClassName});
	}

	@Override
	protected State performActionHelper(State s, String[] params) {
		return super.performActionHelper(s, params);
	}

}
