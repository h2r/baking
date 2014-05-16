package edu.brown.cs.h2r.baking;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class RecipeFinished extends PropositionalFunction {
	
	protected IngredientRecipe topLevelIngredient;
	public RecipeFinished(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {IngredientFactory.ClassNameComplex});
		this.topLevelIngredient = ingredient;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		if (!params[0].equalsIgnoreCase("")) {
			ObjectInstance ingredient = state.getObject(params[0]);
			return Recipe.isSuccess(state, this.topLevelIngredient, ingredient);
		}
		else
		{
			List<ObjectInstance> ingredients = state.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex);
			for (ObjectInstance ingredient: ingredients) {
				if (Recipe.isSuccess(state, this.topLevelIngredient, ingredient)) {
					return true;
				}
			}
		}
		return false;
	}

}
