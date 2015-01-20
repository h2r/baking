package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class AllowPrepare extends BakingPropositionalFunction {

	public AllowPrepare(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, IngredientFactory.ClassNameSimple, ContainerFactory.ClassName}, ingredient);
	}
	@Override
	public BakingPropositionalFunction updatePF(Domain newDomain,
			IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return this;
	}

	@Override
	public boolean isTrue(State s, String[] params) {
		String ingredientName = params[1];
		ObjectInstance ingredient = s.getObject(ingredientName);
		if (IngredientFactory.getPrepTraits(ingredient).isEmpty()) {
			return false;
		}
		
		return true;
	}

}
