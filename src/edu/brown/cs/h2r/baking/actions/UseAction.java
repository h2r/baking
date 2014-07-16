package edu.brown.cs.h2r.baking.actions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;

public class UseAction extends BakingAction {
	public static final String className = "use";
	public UseAction(Domain domain, IngredientRecipe ingredient) {
		super(UseAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ToolFactory.ClassName, ContainerFactory.ClassName});
	}
	
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		String containerSpace = ContainerFactory.getSpaceName(container);
		String toolSpace = ToolFactory.getSpaceName(tool);
		if (!toolSpace.equals(containerSpace)) {
			return BakingActionResult.failure(tool.getName() + " not in same space as " + container.getName());
		}
		
		for (String name : ContainerFactory.getContentNames(container)) {
			ObjectInstance ingredient = state.getObject(name);
			// This tool can't be used on this ingredient
			if (!ToolFactory.toolCanBeUsed(tool, ingredient)) {
				return BakingActionResult.failure(tool.getName() + " can't be used on " + name);
			}
			// Tool has already been used on this ingredient
			if (ToolFactory.toolHasBeenUsed(tool, ingredient)) {
				return BakingActionResult.failure(tool.getName() + " has already been used on " + name);
			}
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
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		if (ToolFactory.toolCanCarry(tool)) {
			this.useTransportableTool(state, tool, container);
		} else {
			this.useSimpleTool(state, tool, container);
		}
		return state;
	}
	
	public void useSimpleTool(State state, ObjectInstance tool, ObjectInstance container) {
		Set<String> contentNames = ContainerFactory.getContentNames(container);
		for (String name : contentNames) {				
			ObjectInstance ingredient = state.getObject(name);
			IngredientFactory.addToolAttribute(ingredient, ToolFactory.getToolAttribute(tool));
		}
	}
	
	public void useTransportableTool(State state, ObjectInstance tool, ObjectInstance container) {
		if (ToolFactory.isEmpty(tool)) {
			Set<String> contentNames = ContainerFactory.getContentNames(container);
			for (String name : contentNames) {
				ObjectInstance ingredient = state.getObject(name);
				IngredientFactory.addToolAttribute(ingredient, ToolFactory.getToolAttribute(tool));
				ToolFactory.addIngredient(tool, ingredient);
			}
		} else {
			ToolFactory.pourIngredients(state, tool, container);
		}
	}
}
