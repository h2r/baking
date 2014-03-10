package edu.brown.cs.h2r.baking.actions;

import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;

public class BakingAction extends Action {

	public BakingAction() {
		// TODO Auto-generated constructor stub
	}

	public BakingAction(String name, Domain domain, String parameterClasses) {
		super(name, domain, parameterClasses);
		// TODO Auto-generated constructor stub
	}

	public BakingAction(String name, Domain domain, String[] parameterClasses) {
		super(name, domain, parameterClasses);
		// TODO Auto-generated constructor stub
	}

	public BakingAction(String name, Domain domain, String[] parameterClasses,
			String[] parameterOrderGroups) {
		super(name, domain, parameterClasses, parameterOrderGroups);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean applicableInState(State state, String[] params) {
		if (params[0] == "human") {
		return true;
		}
		else return this.hasHumanGone(state);
	}

	@Override
	protected State performActionHelper(State s, String[] params) {
		this.addAgentToOccupiedList(s, params[0]);
		return s;	
	}
	
	protected boolean hasHumanGone(State state) {
		List<ObjectInstance> makeSpanObjects = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty()) {
			Set<String> agents = MakeSpanFactory.getOccupiedAgentNames(makeSpanObjects.get(0));
			if (agents.size() == MakeSpanFactory.getAgentCount(makeSpanObjects.get(0))) {
				return false;
			}
			if (agents.contains("human")) {
				return true;
			}
		}
		// If make span is not used here, it's always good.
		return true;
	}
	
	protected boolean checkValidAgent(State state, String agentToCheck) {
		List<ObjectInstance> makeSpanObjects = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty()) {
			return MakeSpanFactory.isAgentIsFree(makeSpanObjects.get(0), agentToCheck);
		}
		// If make span is not used here, it's always good.
		return true;
	}
	
	protected void addAgentToOccupiedList(State state, String agentName) {
		List<ObjectInstance> makeSpanObjects = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty()) {
			MakeSpanFactory.occupyAgent(makeSpanObjects.get(0), agentName);
		}		
	}
}
