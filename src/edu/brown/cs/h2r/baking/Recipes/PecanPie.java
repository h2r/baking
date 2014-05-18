package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class PecanPie extends Recipe {
	
	public PecanPie() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("flour", false, false, false));
		ingredientList.add(new IngredientRecipe("sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("salt", false, false, false));
		ingredientList.add(new IngredientRecipe("butter", false, false, false));
		ingredientList.add(new IngredientRecipe("eggs", false, false, false));
		//ingredientList.add(new IngredientRecipe("pecans", false, false, false));
		//ingredientList.add(new IngredientRecipe("brown_sugar", false, false, false));
		//ingredientList.add(new IngredientRecipe("vanilla_extract", false, false, false));
		//ingredientList.add(new IngredientRecipe("bourbon", false, false, false));
		ingredientList.add(new IngredientRecipe("frozen_dough", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("PecanPie", false, false, false, ingredientList);
		
	}

}
