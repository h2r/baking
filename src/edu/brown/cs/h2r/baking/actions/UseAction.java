package edu.brown.cs.h2r.baking.actions;

import java.util.HashSet;
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
		return BakingActionResult.failure("Don't use this action"); /*
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
	
		
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		String containerSpaceName = ContainerFactory.getSpaceName(container);
		ObjectInstance containerSpace = state.getObject(containerSpaceName);
		String toolSpace = ToolFactory.getSpaceName(tool);
		
		Set<ObjectInstance> containerObjs = new HashSet<ObjectInstance>();
		for (String name : ContainerFactory.getContentNames(container)) {
			ObjectInstance ingredient = state.getObject(name);
			containerObjs.add(ingredient);
		}
		boolean canBeUsed = false;
		if (ToolFactory.toolCanCarry(tool)) {
			if (ToolFactory.isEmpty(tool)) {
				if (ContainerFactory.isEmptyContainer(container)) {
					return BakingActionResult.failure("Can't use " + tool.getName() + " on " + container.getName() + " since it is empty!");
				}
				Set<String> includes = ToolFactory.getIncludes(tool);
				Set<String> excludes = ToolFactory.getExcludes(tool);
				if (!includes.isEmpty()) {
					for (ObjectInstance ingredient : containerObjs) {
						for (String trait : includes) {
							if (IngredientFactory.getTraits(ingredient).contains(trait)) {
								if (ToolFactory.toolCanBeUsed(tool, ingredient) &&
									!ToolFactory.toolHasBeenUsed(tool, ingredient)) {
									canBeUsed = true;
									break;
								}
								return BakingActionResult.failure(tool.getName() + " can't be used on " + ingredient.getName());
							}
						}
					}
					if (canBeUsed) {
						return BakingActionResult.success();
					}
					return BakingActionResult.failure(tool.getName() + " can't be used on any of the ingredients in " + container.getName());
				}
				else if (!excludes.isEmpty()) {
					for (ObjectInstance ingredient : containerObjs) {
						for (String trait : excludes) {
							if (!IngredientFactory.getTraits(ingredient).contains(trait)) {
								if (ToolFactory.toolCanBeUsed(tool, ingredient) &&
									!ToolFactory.toolHasBeenUsed(tool, ingredient)) {
									canBeUsed = true;
									break;
								}
								return BakingActionResult.failure(tool.getName() + " can't be used on " + ingredient.getName());
							}
						}
					}
					if (canBeUsed) {
						return BakingActionResult.success();
					}
					return BakingActionResult.failure(tool.getName() + " can't be used on any of the ingredients in " + container.getName());
				} else {
					for (ObjectInstance ingredient : containerObjs) {
						
						// This tool can't be used on this ingredient
						if (!ToolFactory.toolCanBeUsed(tool, ingredient)) {
							return BakingActionResult.failure(tool.getName() + " can't be used on " + name);
						}
						// Tool has already been used on this ingredient
						if (ToolFactory.toolHasBeenUsed(tool, ingredient)) {
							return BakingActionResult.failure(tool.getName() + " has already been used on " + name);
						}
					}
				}
			} else {
				if (!ContainerFactory.isReceivingContainer(container)) {
					return BakingActionResult.failure(container.getName() + " cannot be poured into");
				}
				if (SpaceFactory.isBaking(containerSpace)) {
					return BakingActionResult.failure(container + " is in the " +  containerSpaceName+ " which is a baking space!");
				}
			}
		} else {
			if (ContainerFactory.isEmptyContainer(container)) {
				return BakingActionResult.failure("Can't use " + tool.getName() + " on " + container.getName() + " since it is empty!");
			}
			for (ObjectInstance ingredient : containerObjs) {
				// This tool can't be used on this ingredient
				if (!ToolFactory.toolCanBeUsed(tool, ingredient)) {
					return BakingActionResult.failure(tool.getName() + " can't be used on " + name);
				}
				// Tool has already been used on this ingredient
				if (ToolFactory.toolHasBeenUsed(tool, ingredient)) {
					return BakingActionResult.failure(tool.getName() + " has already been used on " + name);
				}
			}
		}
		
		return BakingActionResult.success(); */
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		String agent = params[0];
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		if (ToolFactory.toolCanCarry(tool)) {
			this.useTransportableTool(state, tool, container, agent);
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
	
	public void useTransportableTool(State state, ObjectInstance tool, ObjectInstance container, String agent) {
		if (ToolFactory.isEmpty(tool)) {
			Set<String> contentNames = ContainerFactory.getContentNames(container);
			Set<String> includes = ToolFactory.getIncludes(tool);
			Set<String> excludes = ToolFactory.getExcludes(tool);
			
			if (!includes.isEmpty()) {
				// only add the ingredients that have the trait we're trying to includes
				for (String name : contentNames) {
					ObjectInstance ingredient = state.getObject(name);
					for (String trait : includes) {
						if (IngredientFactory.getTraits(ingredient).contains(trait)) {
							ToolFactory.addIngredientToTool(state, tool, ingredient);
							break;
						}
					}
				}
			} else {
				// Add all ingredients but those with the traits we're trying to exclude
				for (String name : contentNames) {
					ObjectInstance ingredient = state.getObject(name);
					boolean exclude = false;
					for (String trait : excludes) {
						if (IngredientFactory.getTraits(ingredient).contains(trait)) {
							exclude = true;
							break;
						}
					}
					if (!exclude) {
						ToolFactory.addIngredientToTool(state, tool, ingredient);
					}
				}	
			}
		} else {
			ToolFactory.pourIngredients(domain, state, tool, container, agent);
		}
	}
}
