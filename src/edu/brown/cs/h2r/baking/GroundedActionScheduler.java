package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.ActionTree.ActionNode;

public class GroundedActionScheduler {
	protected Domain domain;
	protected State initialState;
	protected List<GroundedAction> actions;
	protected List<String> agents;
	
	public GroundedActionScheduler(Domain domain, State initialState, List<GroundedAction> actions, List<String> agents) {
		this.domain = domain;
		this.initialState = initialState;
		this.actions = new ArrayList<GroundedAction>(actions);
		this.agents = new ArrayList<String>(agents);
	}
	
	public Map<String, List<GroundedAction>> getAllocatedTasks() {
	
		Map<String, List<GroundedAction>> allocatedTasks = 
				new HashMap<String, List<GroundedAction>>();
		
		//ActionTree actionTree = new ActionTree(this.initialState);
		//State state = new State(this.initialState);
		
		
		return allocatedTasks;
	}
	
	public List<GroundedAction> createActionNodes(ActionNode rootNode, List<GroundedAction> availableActions) {
		List<GroundedAction> unusedActions = new ArrayList<GroundedAction>();
		State state = rootNode.getState();
		for (GroundedAction action : availableActions) 
		{
			if (action.action.applicableInState(state, action.params)) {
				rootNode.add(action.executeIn(state), action);
			}
			else {
				unusedActions.add(action);
			}
		}
		List<ActionNode> childNodes = rootNode.getChildren();
		for (ActionNode childNode : childNodes) {
			this.createActionNodes(childNode, unusedActions);
		}
		
		return unusedActions;
	}
	
	public boolean volidSomething(State initialState, List<GroundedAction> actions) {
		State state = new State(initialState);
		for (GroundedAction groundedAction : actions) {
			if (!groundedAction.action.applicableInState(state, groundedAction.params)) {
				return false;
			}
			state = groundedAction.executeIn(state);
		}
		return true;
		
	}
}
