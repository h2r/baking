package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class TurnOnOffAction extends BakingAction {
	public TurnOnOffAction(Domain domain) {
		super("turnOnOff", domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName});
	}
}
