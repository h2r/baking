package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;

public class UseAction extends BakingAction {
	public UseAction(Domain domain, IngredientRecipe ingredient) {
		super("use", domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
	}
}
