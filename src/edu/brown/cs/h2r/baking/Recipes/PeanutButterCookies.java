package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;
import edu.brown.cs.h2r.baking.IngredientRecipe;


public class PeanutButterCookies extends Recipe {
	
	public PeanutButterCookies() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("peanut_butter", false, false, false));
		ingredientList.add(new IngredientRecipe("butter", false, false, false));
		ingredientList.add(new IngredientRecipe("brown_sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("white_sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("vanilla_extract", false, false, false));
		ingredientList.add(new IngredientRecipe("eggs", false, false, false));
		ingredientList.add(new IngredientRecipe("flour", false, false, false));
		ingredientList.add(new IngredientRecipe("baking_soda", false, false, false));
		ingredientList.add(new IngredientRecipe("baking_powder", false, false, false));
		ingredientList.add(new IngredientRecipe("salt", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("PeanutButterCookies", false, false, false, ingredientList);
	}
}
