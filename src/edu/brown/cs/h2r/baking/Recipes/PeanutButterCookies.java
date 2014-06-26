package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.IngredientRecipe;


public class PeanutButterCookies extends Recipe {
	
	public PeanutButterCookies() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("peanut_butter"));
		IngredientRecipe creamed = new IngredientRecipe("creamed_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		creamed.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(creamed);
		ingredientList2.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe wet_ingredients = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(wet_ingredients);
		ingredientList3.add(knowledgebase.getIngredient("baking_soda"));
		ingredientList3.add(knowledgebase.getIngredient("baking_powder"));
		IngredientRecipe cookies = new IngredientRecipe("peanutButterCookies", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);
		cookies.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		cookies.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		this.topLevelIngredient = cookies;
	}
	
	public void setUpSubgoals(Domain domain) {
		
	}
}
