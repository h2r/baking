package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class MoveAction extends BakingAction {
	public static final String className = "move";
	public MoveAction(Domain domain, IngredientRecipe ingredient) {
		super("move", domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName});
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
		String spaceName = params[2];
		ObjectInstance space = state.getObject(spaceName);
		String agentName = SpaceFactory.getAgent(space).iterator().next();
		String paramAgentName = params[0];
		if (!agentName.isEmpty() && !agentName.equalsIgnoreCase(paramAgentName)) {
			return BakingActionResult.failure(paramAgentName + " cannot move objects to the " + spaceName);
		}
		
		String containerName = params[1];
		ObjectInstance container = state.getObject(containerName);
		if (ContainerFactory.getSpaceName(container).equals(spaceName)) {
			return BakingActionResult.failure(containerName + " is already in " + spaceName);
		}
		
		if (SpaceFactory.isBaking(space) && !ContainerFactory.isBakingContainer(container)) {
			return BakingActionResult.failure(spaceName + " can only contain baking containers");
		}
		
		if (SpaceFactory.isHeating(space) && !ContainerFactory.isHeatingContainer(container)) {
			return BakingActionResult.failure(spaceName + " can only contain heating containers");
		}
		
		return BakingActionResult.success();		
	}
	
	@Override
	public boolean applicableInState(State s, String[] params) {
		return this.checkActionIsApplicableInState(s, params).getIsSuccess();
	
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		//System.out.println("Moving container " + params[1] + " to " + params[2]);
		ObjectInstance containerInstance = state.getObject(params[1]);
		ObjectInstance spaceInstance = state.getObject(params[2]);
		move (state, containerInstance, spaceInstance);
		return state;
	}
	
	public static void move(State state, ObjectInstance containerInstance, ObjectInstance spaceInstance) {
		String oldSpace = ContainerFactory.getSpaceName(containerInstance);
		ObjectInstance oldSpaceObject = state.getObject(oldSpace);
		ContainerFactory.changeContainerSpace(containerInstance, spaceInstance.getName());
		SpaceFactory.addContainer(spaceInstance, containerInstance);
		SpaceFactory.removeContainer(oldSpaceObject, containerInstance);
		
		if (SpaceFactory.getOnOff(spaceInstance) && !ContainerFactory.isEmptyContainer(containerInstance)) {
			if (SpaceFactory.isHeating(spaceInstance) && ContainerFactory.isHeatingContainer(containerInstance)) {
				for (String name : ContainerFactory.getContentNames(containerInstance)) {
					if (!IngredientFactory.isMeltedAtRoomTemperature(state.getObject(name))) {
						IngredientFactory.meltIngredient(state.getObject(name));
					}
				}
			}
			else if (SpaceFactory.isBaking(spaceInstance) && ContainerFactory.isBakingContainer(containerInstance)) {
				for (String name : ContainerFactory.getContentNames(containerInstance)) {
					IngredientFactory.bakeIngredient(state.getObject(name));
				}
			}
		}
	}
}