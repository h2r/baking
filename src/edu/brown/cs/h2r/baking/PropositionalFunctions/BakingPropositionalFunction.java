package edu.brown.cs.h2r.baking.PropositionalFunctions;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class BakingPropositionalFunction extends PropositionalFunction {

	protected IngredientRecipe topLevelIngredient;
	public BakingPropositionalFunction(String name, Domain domain, String[] params, IngredientRecipe ingredient) {
		super(name, domain, params);
		this.topLevelIngredient = ingredient;
	}
	@Override
	public boolean isTrue(State s, String[] params) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void changeTopLevelIngredient(IngredientRecipe new_ingredient) {
		this.topLevelIngredient = new_ingredient;
	}

}
