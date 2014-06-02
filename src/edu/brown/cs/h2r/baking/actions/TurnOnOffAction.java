package edu.brown.cs.h2r.baking.actions;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class TurnOnOffAction extends BakingAction {
	public TurnOnOffAction(Domain domain, IngredientRecipe ingredient) {
		super("turnOnOff", domain, ingredient, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName});
	}
}
