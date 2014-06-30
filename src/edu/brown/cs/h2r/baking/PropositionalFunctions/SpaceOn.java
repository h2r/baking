package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class SpaceOn extends BakingPropositionalFunction {

	String spaceName;
	public SpaceOn(String name, Domain domain, IngredientRecipe ingredient, String space) {
		super(name, domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName} ,ingredient);
		this.spaceName = space;
	}
	
	public boolean isTrue(State state, String[] params) {
		
		if ((this.spaceName != null && !this.spaceName.equals("")) && !this.spaceName.equals(params[1])) {
			return false;
		}
		ObjectInstance space = state.getObject(params[1]);
		return SpaceFactory.getOnOff(space);
	}
}
