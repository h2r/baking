package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class DeviledEggs extends Recipe {

	public DeviledEggs() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("egg_yolks", false, false, false));
		ingredientList.add(new IngredientRecipe("egg_whites", false, false, false));
		ingredientList.add(new IngredientRecipe("salt", false, false, false));
		ingredientList.add(new IngredientRecipe("pepper", false, false, false));
		ingredientList.add(new IngredientRecipe("mustard", false, false, false));
		ingredientList.add(new IngredientRecipe("tarragon", false, false, false));
		ingredientList.add(new IngredientRecipe("pickles", false, false, false));
		ingredientList.add(new IngredientRecipe("shallots", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("DeviledEggs", false, false, false, ingredientList);

	}
}
