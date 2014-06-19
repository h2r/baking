package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class MoveAction extends BakingAction {
	public static final String className = "move";
	public MoveAction(Domain domain, IngredientRecipe ingredient) {
		super("move", domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName});
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
			return false;
		}
		ObjectInstance container = s.getObject(params[1]);
		if (ContainerFactory.getSpaceName(container).equals(spaceName)) {
			return false;
		}
		
		return true;
	
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
	
	protected void move(State state, ObjectInstance containerInstance, ObjectInstance spaceInstance) {
		ContainerFactory.changeContainerSpace(containerInstance, spaceInstance.getName());
		SpaceFactory.addContainer(spaceInstance, containerInstance);
	}
}