package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class AllowGreasing extends BakingPropositionalFunction {
	public AllowGreasing(String name, Domain domain,  IngredientRecipe ingredient, Recipe recipe) {
		super(name, domain, new String[] {AgentFactory.ClassName},ingredient);
	}

	public boolean isTrue(State state, String[] params) {
		return true;
	}
}
