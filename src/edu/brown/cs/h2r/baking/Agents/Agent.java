package edu.brown.cs.h2r.baking.Agents;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

public interface Agent {
	AbstractGroundedAction getAction(State state);
}
