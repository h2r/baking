package edu.brown.cs.h2r.baking.PropositionalFunctions;

import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public abstract class BakingPropositionalFunction extends PropositionalFunction {

	protected final IngredientRecipe topLevelIngredient;
	protected final BakingSubgoal subgoal;
	public BakingPropositionalFunction(String name, Domain domain, String[] params, IngredientRecipe ingredient) {
		super(name, domain, params);
		this.topLevelIngredient = ingredient;
		this.subgoal = null;
	}
	
	public BakingPropositionalFunction(BakingPropositionalFunction pf, Domain domain) {
		super(pf, domain);
		this.topLevelIngredient = pf.topLevelIngredient;
		this.subgoal = pf.subgoal;
	}
	
	public BakingPropositionalFunction(BakingPropositionalFunction pf, Domain domain, IngredientRecipe ingredient, BakingSubgoal goal) {
		super(pf, domain);
		this.topLevelIngredient = ingredient;
		this.subgoal = goal;
	}
	
	public abstract BakingPropositionalFunction updatePF(Domain newDomain, IngredientRecipe ingredient, BakingSubgoal subgoal);
	
	public IngredientRecipe getTopLevelIngredient() {
		return this.topLevelIngredient;
	}

}
