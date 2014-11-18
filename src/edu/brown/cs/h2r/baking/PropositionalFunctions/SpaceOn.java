package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class SpaceOn extends BakingPropositionalFunction {

	private final String spaceName;
	public SpaceOn(String name, Domain domain, IngredientRecipe ingredient, String space) {
		super(name, domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName} ,ingredient);
		this.spaceName = space;
	}
	
	public BakingPropositionalFunction updatePF(Domain newDomain, IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return new SpaceOn(this.name, newDomain, ingredient, this.spaceName);
	}
	
	public boolean isTrue(State state, String[] params) {
		
		if (!this.spaceName.equals(params[1])) {
			return false;
		}
		ObjectInstance space = state.getObject(this.spaceName);
		return SpaceFactory.getOnOff(space);
	}
	
	@Override 
	public String toString() {
		return "Space on : " + this.spaceName;
	}
}
