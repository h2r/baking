package edu.brown.cs.h2r.baking.actions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
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
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	
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
	
	private static void move(State state, ObjectInstance containerInstance, ObjectInstance spaceInstance) {
		String oldSpace = ContainerFactory.getSpaceName(containerInstance);
		ObjectInstance oldSpaceObject = state.getObject(oldSpace);
		ContainerFactory.changeContainerSpace(containerInstance, spaceInstance.getName());
		SpaceFactory.addContainer(spaceInstance, containerInstance);
		SpaceFactory.removeContainer(oldSpaceObject, containerInstance);
		
		if (SpaceFactory.getOnOff(spaceInstance)) {
			if (SpaceFactory.isBaking(spaceInstance)) {
				MoveAction.movingToBakingSpace(state, spaceInstance, containerInstance);
			}
			if (SpaceFactory.isHeating(spaceInstance)) {
				MoveAction.movingToHeatingSpace(state, spaceInstance, containerInstance);
			}
		}
	}
	
	private static void movingToBakingSpace(State state, ObjectInstance spaceInstance, ObjectInstance containerInstance) {
		if (!ContainerFactory.isEmptyContainer(containerInstance) && ContainerFactory.isBakingContainer(containerInstance)) {
			Set<String> names = ContainerFactory.getContentNames(containerInstance);
			for (String name : names) {
				ObjectInstance ing = state.getObject(name);
				IngredientFactory.bakeIngredient(ing);
			}
		}
	}
	
	private static void movingToHeatingSpace(State state, ObjectInstance spaceInstance, ObjectInstance containerInstance) {
		if (!ContainerFactory.isEmptyContainer(containerInstance) && ContainerFactory.isHeatingContainer(containerInstance)) {
			/*Set<String> names = ContainerFactory.getContentNames(containerInstance);
			for (String name : names) {
				ObjectInstance ing = state.getObject(name);
				if (!IngredientFactory.isMeltedAtRoomTemperature(ing)) {
					Knowledgebase.heatIngredient(container, ing);
				}
			}*/
			Knowledgebase.heatContainer(state, containerInstance);
		}
	}
}