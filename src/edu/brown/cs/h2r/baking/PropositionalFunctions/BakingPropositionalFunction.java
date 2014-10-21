package edu.brown.cs.h2r.baking.PropositionalFunctions;

import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class BakingPropositionalFunction extends PropositionalFunction {

	protected IngredientRecipe topLevelIngredient;
	protected BakingSubgoal subgoal;
	public BakingPropositionalFunction(String name, Domain domain, String[] params, IngredientRecipe ingredient) {
		super(name, domain, params);
		this.topLevelIngredient = ingredient;
	}
	
	@Override
	public boolean isTrue(State state, String[] params) {
		return false;
	}
	
	public void changeTopLevelIngredient(IngredientRecipe newIngredient) {
		this.topLevelIngredient = newIngredient;
	}
	
	public IngredientRecipe getTopLevelIngredient() {
		return this.topLevelIngredient;
	}
	
	public void setSubgoal(BakingSubgoal sg) {
		this.subgoal = sg;
	}

}
