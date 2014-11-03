package edu.brown.cs.h2r.baking.Agents;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public interface Agent {
	
	void addObservation(State state, GroundedAction action);
	String getAgentName();
	ObjectInstance getAgentObject();
	void setInitialState(State state);
	AbstractGroundedAction getAction(State state);
}
