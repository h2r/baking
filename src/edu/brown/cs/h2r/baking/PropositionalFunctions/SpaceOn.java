package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class SpaceOn extends BakingPropositionalFunction {

	public SpaceOn(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName} ,ingredient);
	}
	
	public boolean isTrue(State state, String[] params) {
		ObjectInstance space = state.getObject(params[1]);
		return SpaceFactory.getOnOff(space);
	}
}