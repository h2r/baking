package edu.brown.cs.h2r.baking.actions;
import java.util.Arrays;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;

public class MoveAction extends BakingAction {
	public static final String className = "move";
	public MoveAction(Domain domain) {
		super("move", domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State s, String[] params) {
		if (!super.applicableInState(s, params)) {
			return false;
		}
		String spaceName = params[2];
		ObjectInstance space = s.getObject(spaceName);
		String agentName = SpaceFactory.getAgent(space).iterator().next();
		if (agentName != params[0]) {
			//return false;
		}
		return true;
	
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		//System.out.println("Moving container " + params[1] + " to " + params[2]);
		ObjectInstance containerInstance = state.getObject(params[1]);
		ObjectInstance spaceInstance = state.getObject(params[2]);
		ContainerFactory.changeContainerSpace(containerInstance, spaceInstance.getName());
		SpaceFactory.addContainer(spaceInstance, containerInstance);
		return state;
	}
}