package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class AllowUsingTool extends BakingPropositionalFunction {

	private Recipe recipe;
	public AllowUsingTool(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ToolFactory.ClassName, ContainerFactory.ClassName} ,ingredient);
		this.recipe = null;
	}
	
	public boolean isTrue(State state, String[] params) {
		ObjectInstance tool = state.getObject(params[1]);
		return this.recipe.getRecipeToolAttributes().contains(ToolFactory.getToolAttribute(tool));
	}
	
	public void addRecipe(Recipe recipe) {
		this.recipe = recipe;
	}
}
