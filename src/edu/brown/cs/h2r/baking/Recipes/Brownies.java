package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class Brownies extends Recipe {
	
	public Brownies() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("Cocoa", false, false, false));
		ingredientList.add(new IngredientRecipe("Baking soda", false, false, false));
		//ingredientList.add(new IngredientRecipe("Baking powder", false, false, false));
		//ingredientList.add(new SimpleIngredient("Eggs", false, false, false));
		//ingredientList.add(new SimpleIngredient("Butter", false, false, false));
		//ingredientList.add(new SimpleIngredient("Flour", false, false, false));
		//ingredientList.add(new SimpleIngredient("Sugar", false, false, false));
		//ingredientList.add(new SimpleIngredient("Salt", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList);
	}
}
