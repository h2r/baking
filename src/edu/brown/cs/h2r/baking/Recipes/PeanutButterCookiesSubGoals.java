package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class PeanutButterCookiesSubGoals extends Recipe {
	
	public PeanutButterCookiesSubGoals() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("peanut_butter", false, false, false));
		ingredientList.add(new IngredientRecipe("butter", false, false, false));
		ingredientList.add(new IngredientRecipe("brown_sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("white_sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("egss", false, false, false));
		IngredientRecipe ingredient1 = new IngredientRecipe("goal1", false, false, false, ingredientList);
		
		//List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		//ingredientList2.add(ingredient1);
		//ingredientList2.add(new IngredientRecipe("eggs", false, false, false));
		//IngredientRecipe ingredient2 = new IngredientRecipe("goal2", false, false, false, ingredientList2);

		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(new IngredientRecipe("salt", false, false, false));
		ingredientList3.add(new IngredientRecipe("flour", false, false, false));
		ingredientList3.add(new IngredientRecipe("baking_powder", false, false, false));
		ingredientList3.add(new IngredientRecipe("baking_soda", false, false, false));
		IngredientRecipe ingredient3 = new IngredientRecipe("goal3", false, false, false, ingredientList3);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(ingredient1);
		ingredientList4.add(ingredient3);
		this.topLevelIngredient = new IngredientRecipe("PeanutButterCookies", false, false, false, ingredientList4);
	}

}
