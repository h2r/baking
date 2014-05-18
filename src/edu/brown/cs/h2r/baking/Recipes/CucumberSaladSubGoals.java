package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class CucumberSaladSubGoals extends Recipe {
	
	public CucumberSaladSubGoals() {
		
		//List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		//ingredientList.add(new IngredientRecipe("olive_oil", false, false, false));
		//ingredientList.add(new IngredientRecipe("salt", false, false, false));
		//ingredientList.add(new IngredientRecipe("pepper", false, false, false));
		//IngredientRecipe ingredient1 = new IngredientRecipe("goal1", false, false, false, ingredientList);

		
		/**
		 * When asked for limes, will the user prefer fresh limes, prepared lime
		 * juice, or even use lemons? 
		 */
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		IngredientRecipe lime = new IngredientRecipe("lime", false, false, false);
		/*lime.setOptions(true);
		lime.addToOptionsList(new IngredientRecipe("limes", false, false, false));
		lime.addToOptionsList(new IngredientRecipe("lime_juice", false, false, false));*/
		//lime.addToOptionsList(new IngredientRecipe("lemons", false, false, false));
		ingredientList2.add(lime);
		//IngredientRecipe ingredient2 = new IngredientRecipe("goal2", false, false, false, ingredientList2);
		
		//List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		//ingredientList3.add(new IngredientRecipe("cucumbers", false, false, false));
		//ingredientList3.add(new IngredientRecipe("onions", false, false, false));
		//ingredientList3.add(new IngredientRecipe("tomatoes", false, false, false));
		//ingredientList3.add(ingredient1);
		//ingredientList3.add(ingredient2);
		//IngredientRecipe ingredient3 = new IngredientRecipe("goal3", false, false, false, ingredientList3);

		
		
		//List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		//ingredientList4.add(ingredient1);
		//ingredientList4.add(ingredient2);
		this.topLevelIngredient = new IngredientRecipe("CucumberSalad", false, false, false, ingredientList2);
		
	}

}
