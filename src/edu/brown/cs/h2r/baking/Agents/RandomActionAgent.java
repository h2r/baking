package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;

public class RandomActionAgent extends Agent {
	private final Domain domain;
	private final NameDependentStateHashFactory hashingFactory = new NameDependentStateHashFactory();
	public RandomActionAgent(Domain domain) {
		super("partner");
		this.domain = domain;
	}
	
	@Override
	protected Map<String, Object> toMap() {
		return super.toMap();
	}
	@Override
	public void addObservation(State state) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

	@Override
	public void setInitialState(State state) {}

	@Override
	public AbstractGroundedAction getAction(State state) {
		List<Action> actions = this.domain.getActions();
		List<GroundedAction> groundedActions = new ArrayList<GroundedAction>();
		for (Action action : actions) {
			groundedActions.addAll(action.getAllApplicableGroundedActions(state));
		}
		if (groundedActions.size() == 0) {
			//System.err.println("Grounded actions for random agent is 0");
		}
		Collections.shuffle(groundedActions);
		
		for (GroundedAction groundedAction : groundedActions) {
			if (groundedAction.params[0].equals(this.getAgentName())) {
				return groundedAction;
			}
		}
		return null;
	}
	
	@Override
	public AbstractGroundedAction getActionWithScheduler(State state, List<String> agents, boolean finishRecipe) {
		return this.getAction(state);
	}

	@Override
	public void reset() {}
	
	public void performResetAction() {}
}
