package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class CucumberSalad extends Recipe {
	
	public CucumberSalad() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("limes", false, false, false));
		ingredientList.add(new IngredientRecipe("lime_juice", false, false, false));
		//ingredientList.add(new IngredientRecipe("lemons", false, false, false));
		//ingredientList.add(new IngredientRecipe("salt", false, false, false));
		//ingredientList.add(new IngredientRecipe("pepper", false, false, false));
		//ingredientList.add(new IngredientRecipe("olive_oil", false, false, false));
		//ingredientList.add(new IngredientRecipe("onions", false, false, false));
		//ingredientList.add(new IngredientRecipe("tomatoes", false, false, false));
		//ingredientList.add(new IngredientRecipe("cucumber", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("CucumberSalad", false, false, false, ingredientList);
		
	}
}
