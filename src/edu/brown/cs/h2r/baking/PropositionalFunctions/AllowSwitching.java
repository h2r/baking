package edu.brown.cs.h2r.baking.PropositionalFunctions;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;

public class AllowSwitching extends BakingPropositionalFunction {

	public AllowSwitching(String name, Domain domain, IngredientRecipe ingredient, Recipe recipe) {
		super(name, domain, new String[] {AgentFactory.ClassName}, ingredient);
	}
	
	public boolean isTrue(State state, String[] params[]) {
		return true;
	}
}
