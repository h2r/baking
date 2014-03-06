package edu.brown.cs.h2r.baking;
import java.util.List;

import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;


public class RecipeBotched extends PropositionalFunction {

	protected IngredientRecipe topLevelIngredient;
	public RecipeBotched(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {IngredientFactory.ClassNameComplex});
		this.topLevelIngredient = ingredient;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance ingredient = state.getObject(params[0]);
		return Recipe.isFailure(state, this.topLevelIngredient, ingredient);
	}

}
