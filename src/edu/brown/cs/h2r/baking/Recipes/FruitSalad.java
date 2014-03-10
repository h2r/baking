package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class FruitSalad extends Recipe {
	
	public FruitSalad() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("water", false, false, false));
		ingredientList.add(new IngredientRecipe("salt", false, false, false));
		//ingredientList.add(new IngredientRecipe("apples", false, false, false));
		//ingredientList.add(new IngredientRecipe("grapes", false, false, false));
		//ingredientList.add(new IngredientRecipe("yogurt", false, false, false));
		//ingredientList.add(new SimpleIngredient("flour", false, false, false));
		//ingredientList.add(new SimpleIngredient("sugar", false, false, false));
		//ingredientList.add(new SimpleIngredient("salt", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("salted_water", false, false, false, ingredientList);
	}
}
