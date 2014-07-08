package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;

public class UseAction extends BakingAction {
	public static final String className = "use";
	public UseAction(Domain domain, IngredientRecipe ingredient) {
		super(UseAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ToolFactory.ClassName, ContainerFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		
		if (!ToolFactory.getSpaceName(tool).equals(ContainerFactory.getSpaceName(container))) {
			return false;
		}
		
		for (String name : ContainerFactory.getContentNames(container)) {
			ObjectInstance ingredient = state.getObject(name);
			// This tool can't be used on this ingredient
			if (!ToolFactory.toolCanBeUsed(tool, ingredient)) {
				return false;
			}
			// Tool has already been used on this ingredient
			if (ToolFactory.toolHasBeenUsed(tool, ingredient)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		for (String name : ContainerFactory.getContentNames(container)) {
			ObjectInstance ingredient = state.getObject(name);
			this.useTool(state, tool, ingredient);
		}
		
		return state;
	}
	
	public void useTool(State state, ObjectInstance tool, ObjectInstance ingredient) {
		IngredientFactory.addToolAttribute(ingredient, ToolFactory.getToolAttribute(tool));
	}
}
