package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
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
				
		String spaceName = params[2];
		ObjectInstance space = state.getObject(spaceName);
		String agentName = SpaceFactory.getAgent(space).iterator().next();
		String paramAgentName = params[0];
		//if (!agentName.isEmpty() && !agentName.equalsIgnoreCase(paramAgentName)) {
		//	return BakingActionResult.failure(paramAgentName + " cannot move objects to the " + spaceName);
		//}
		
		String toolName = params[1];
		ObjectInstance tool = state.getObject(toolName);
		String toolSpaceName = ToolFactory.getSpaceName(tool);
		ObjectInstance toolSpace = state.getObject(toolSpaceName);
		//if (AgentFactory.isRobot(state.getObject(params[0]))) {
		//	if (!toolSpaceName.equals(SpaceFactory.SPACE_ROBOT)) {
		//		return BakingActionResult.failure(toolName + " not in " + SpaceFactory.SPACE_ROBOT);
		//	}
		//}
		
		//if (SpaceFactory.isCleaning(toolSpace)) {
		//	return BakingActionResult.failure(toolName + " cannot be moved out of " + toolSpaceName);
		//}
		
		if (toolSpaceName.equals(spaceName)) {
			return BakingActionResult.failure(toolName + " is already in " + spaceName);
		}
		
		return BakingActionResult.success();		
	}
	
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance toolInstance = state.getObject(params[1]);
		this.hand(state, toolInstance, params[2]);
		return state;
	}
	
	private void hand(State state, ObjectInstance tool, String space) {
		ToolFactory.changeToolSpace(tool, space);
	}

}