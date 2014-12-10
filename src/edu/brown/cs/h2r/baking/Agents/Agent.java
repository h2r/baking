package edu.brown.cs.h2r.baking.Agents;

import java.util.List;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public interface Agent {
	
	void addObservation(State state);
	String getAgentName();
	ObjectInstance getAgentObject();
	void setInitialState(State state);
	AbstractGroundedAction getAction(State state);
	AbstractGroundedAction getActionWithScheduler(State state, List<String> agents);
}
