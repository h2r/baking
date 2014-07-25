package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;

public class HandAction extends BakingAction {
	public static final String className = "hand";
	public HandAction(Domain domain, IngredientRecipe ingredient) {
		super(HandAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ToolFactory.ClassName, SpaceFactory.ClassName});
	}
	
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
		/*if (!AgentFactory.isRobot(state.getObject(params[0]))) {
			return BakingActionResult.failure("only robots can hand!");
		}*/
		
		String spaceName = params[2];
		ObjectInstance space = state.getObject(spaceName);

		String toolName = params[1];
		ObjectInstance tool = state.getObject(toolName);
		String toolSpaceName = ContainerFactory.getSpaceName(tool);
		
		if (AgentFactory.isRobot(state.getObject(params[0]))) {
			if (!toolSpaceName.equals(SpaceFactory.SPACE_ROBOT)) {
				return BakingActionResult.failure(toolName + " not in " + SpaceFactory.SPACE_ROBOT);
			}
		}
				
		if (toolSpaceName.equals(SpaceFactory.SPACE_DIRTY)) {
			return BakingActionResult.failure(toolName + " is already in " + spaceName);
		}
		
		if (toolSpaceName.equals(spaceName)) {
			return BakingActionResult.failure(toolName + " is already in " + spaceName);
		}
		
		return BakingActionResult.success();		
	}
	
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance toolInstance = state.getObject(params[1]);
		ObjectInstance spaceInstance = state.getObject(params[2]);
		this.hand(state, toolInstance, spaceInstance);
		return state;
	}
	
	private void hand(State state, ObjectInstance tool, ObjectInstance space) {
		ToolFactory.setUsed(tool);
		ToolFactory.changetoolSpace(tool, space.getName());
	}

}
