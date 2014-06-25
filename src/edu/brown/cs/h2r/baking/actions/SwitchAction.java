package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class SwitchAction extends BakingAction {
	public static final String className = "switch";
	public SwitchAction(Domain domain) {
		super(SwitchAction.className, domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		
		ObjectInstance agent =  state.getObject(params[0]);
		
		ObjectInstance spaceInstance = state.getObject(params[1]);
		if (!SpaceFactory.isSwitchable(spaceInstance)) {
			return false;
		}
		
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance spaceInstance = state.getObject(params[1]);
		this.switchOnOff(spaceInstance);
		return state;
	}
	
	protected void switchOnOff(ObjectInstance objectInstance) {
		boolean isOn = SpaceFactory.getOnOff(objectInstance);
		SpaceFactory.setOnOff(objectInstance, !isOn);
	}
}
