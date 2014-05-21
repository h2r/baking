package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Brownies",
				"Preheat oven to 350 degrees F (175 degrees C).\n",						//0
				"Grease and flour an 8-inch square pan.\n",								//1
				"In a large saucepan, melt 1/2 cup butter.\n",							//2
				"Stir in sugar, eggs, and 1 teaspoon vanilla.\n",							//3
				"Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.\n",		//4
				"Spread batter into prepared pan.\n",										//5
				"Bake in preheated oven for 25 to 30 minutes. Do not overcook.");		
	}
}
