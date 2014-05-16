package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class Brownies extends Recipe {
	
	public Brownies() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("Cocoa", false, false, false));
		ingredientList.add(new IngredientRecipe("Baking_soda", false, false, false));
		ingredientList.add(new IngredientRecipe("Baking_powder", false, false, false));
		ingredientList.add(new IngredientRecipe("Eggs", false, false, false));
		ingredientList.add(new IngredientRecipe("Butter", false, false, false));
		ingredientList.add(new IngredientRecipe("Flour", false, false, false));
		ingredientList.add(new IngredientRecipe("Sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("Salt", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList);
	}
}
