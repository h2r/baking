package edu.brown.cs.h2r.baking.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.StateBuilder;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class MoveAction extends BakingAction {
	public static final String className = "move";
	public MoveAction(Domain domain) {
		super("move", domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName});
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
		
		String containerSpaceName = ContainerFactory.getSpaceName(container);		
		if (containerSpaceName.equals(spaceName)) {
			return BakingActionResult.failure(containerName + " is already in " + spaceName);
		}
		
		return BakingActionResult.success();		
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	
	}
	
	@Override
	public String[] getUsedObjects(State state, String[] params) {
		ObjectInstance containerInstance = state.getObject(params[1]);
		String oldSpace = ContainerFactory.getSpaceName(containerInstance);
		List<String> usedObjects =  new ArrayList<String>(Arrays.asList(params));
		usedObjects.add(oldSpace);
		String[] usedObjectsArray = new String[usedObjects.size()];
		return usedObjects.toArray(usedObjectsArray);
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		StateBuilder builder = new StateBuilder(state);
		this.addAgentToOccupiedList(state, builder, params[0]);
		ObjectInstance containerInstance = state.getObject(params[1]);
		ObjectInstance spaceInstance = state.getObject(params[2]);
		return move (state, builder, containerInstance, spaceInstance);
	}
	
	private static State move(State state, StateBuilder builder, ObjectInstance containerInstance, ObjectInstance spaceInstance) {
		String oldSpace = ContainerFactory.getSpaceName(containerInstance);
		ObjectInstance oldSpaceObject = state.getObject(oldSpace);
		
		ObjectInstance newContainer = ContainerFactory.changeContainerSpace(containerInstance, spaceInstance.getName());
		ObjectInstance newSpace = SpaceFactory.addContainer(spaceInstance, containerInstance);
		ObjectInstance newOldSpace = SpaceFactory.removeContainer(oldSpaceObject, containerInstance);
		
		builder.replaceAll(
				Arrays.asList(containerInstance, spaceInstance, oldSpaceObject),
				Arrays.asList(newContainer, newSpace, newOldSpace));
		
		if (SpaceFactory.getOnOff(spaceInstance)) {
			if (SpaceFactory.isBaking(spaceInstance)) {
				builder = MoveAction.movingToBakingSpace(state, builder, spaceInstance, containerInstance);
			}
			if (SpaceFactory.isHeating(spaceInstance)) {
				return MoveAction.movingToHeatingSpace(builder.toState(), spaceInstance, containerInstance);
			}
		}
		return builder.toState();
	}
	
	private static StateBuilder movingToBakingSpace(State state, StateBuilder builder, ObjectInstance spaceInstance, ObjectInstance containerInstance) {
		if (!ContainerFactory.isEmptyContainer(containerInstance) && ContainerFactory.isBakingContainer(containerInstance)) {
			Set<String> names = ContainerFactory.getContentNames(containerInstance);
			List<ObjectInstance> newObjects = new ArrayList<ObjectInstance>();
			List<ObjectInstance> oldObjects = new ArrayList<ObjectInstance>();
			
			for (String name : names) {
				ObjectInstance ing = state.getObject(name);
				oldObjects.add(ing);
				
				ObjectInstance newIng = IngredientFactory.bakeIngredient(ing);
				newObjects.add(newIng);
			}
			
			builder.replaceAll(oldObjects, newObjects);
		}
		return builder;
	}
	
	private static State movingToHeatingSpace(State state, ObjectInstance spaceInstance, ObjectInstance containerInstance) {
		if (!ContainerFactory.isEmptyContainer(containerInstance) && ContainerFactory.isHeatingContainer(containerInstance)) {
			state = Knowledgebase.heatContainer(state, containerInstance);
		}
		return state;
	}
}