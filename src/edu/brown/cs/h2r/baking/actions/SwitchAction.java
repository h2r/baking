package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class SwitchAction extends BakingAction {
	public static final String className = "switch";
	public SwitchAction(Domain domain) {
		super(SwitchAction.className, domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName});
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
		ObjectInstance agent =  state.getObject(params[0]);
		
		String spaceName = params[1];
		ObjectInstance spaceInstance = state.getObject(spaceName);
		if (!SpaceFactory.isSwitchable(spaceInstance)) {
			return BakingActionResult.failure(spaceName + " cannot be turned on/off");
		}
		
		return BakingActionResult.success();
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance spaceInstance = state.getObject(params[1]);
		this.switchOnOff(state, spaceInstance);
		return state;
	}
	
	public void switchOnOff(State state, ObjectInstance space) {
		boolean isOn = SpaceFactory.getOnOff(space);
		SpaceFactory.setOnOff(space, !isOn);
		
		// Space wasn't turned out before
		if (!isOn) {
			for (String name : SpaceFactory.getContents(space)) {
				ObjectInstance container = state.getObject(name);
				if (!ContainerFactory.isEmptyContainer(container)) {
					if (SpaceFactory.isBaking(space) && ContainerFactory.isBakingContainer(container)) {
						for (String ing : ContainerFactory.getContentNames(container)) {
							IngredientFactory.bakeIngredient(state.getObject(ing));
						}
					}
					else if (SpaceFactory.isHeating(space) && ContainerFactory.isHeatingContainer(container)) {
						for (String ing : ContainerFactory.getContentNames(container)) {
							if (!IngredientFactory.isMeltedAtRoomTemperature(state.getObject(ing))) {
								IngredientFactory.meltIngredient(state.getObject(ing));
							}
						}
					}
				}
			}
		}
	}
}
