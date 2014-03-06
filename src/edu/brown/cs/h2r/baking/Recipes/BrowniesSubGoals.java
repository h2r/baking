package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class BrowniesSubGoals extends Recipe {
	
	public BrowniesSubGoals() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("cocoa", false, false, false));
		ingredientList.add(new IngredientRecipe("eggs", false, false, false));
		//ingredientList.add(new IngredientRecipe("sugar", false, false, false));
		IngredientRecipe ingredient1 = new IngredientRecipe("goal1", false, false, false, ingredientList);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(new IngredientRecipe("salt", false, false, false));
		ingredientList2.add(new IngredientRecipe("butter", false, false, false));
		//ingredientList2.add(new IngredientRecipe("flour", false, false, false));
		IngredientRecipe ingredient2 = new IngredientRecipe("goal2", false, false, false, ingredientList2);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(ingredient1);
		ingredientList3.add(ingredient2);
		//ingredientList3.add(new IngredientRecipe("baking_soda", false, false, false));
		//ingredientList3.add(new IngredientRecipe("baking_powder", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList3);
	}
}
