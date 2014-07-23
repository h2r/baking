package edu.brown.cs.h2r.baking.PropositionalFunctions;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class RecipeFinished extends BakingPropositionalFunction {
	
	public RecipeFinished(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName}, ingredient);
	}

	@Override
	public boolean isTrue(State state, String[] params) {

		List<ObjectInstance> ingredients = state.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex);
		ingredients.addAll(state.getObjectsOfTrueClass(IngredientFactory.ClassNameComplexHidden));
		ingredients.addAll(state.getObjectsOfTrueClass(IngredientFactory.ClassNameSimple));
		for (ObjectInstance ingredient: ingredients) {
			if (Recipe.isSuccess(state, this.topLevelIngredient, ingredient)) {
				return true;
			}
		}
		//}
		return false;
	}

}
