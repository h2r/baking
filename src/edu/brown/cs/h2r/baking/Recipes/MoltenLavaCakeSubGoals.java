package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class MoltenLavaCakeSubGoals extends Recipe {

	public MoltenLavaCakeSubGoals() {
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("butter", false, false, false));
		ingredientList.add(new IngredientRecipe("chocolate", false, false, false));
		IngredientRecipe ingredient1 = new IngredientRecipe("goal1", false, false, false, ingredientList);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(new IngredientRecipe("flour", false, false, false));
		ingredientList2.add(new IngredientRecipe("sugar", false, false, false));
		ingredientList2.add(new IngredientRecipe("egg_yolks", false, false, false));
		ingredientList2.add(new IngredientRecipe("eggs", false, false, false));
		ingredientList2.add(new IngredientRecipe("vanilla_extract", false, false, false));
		ingredientList2.add(ingredient1);
		//IngredientRecipe ingredient2 = new IngredientRecipe("goal2", false, false, false, ingredientList2);

		/**
		 * Will the user want to grease the pans with butter, nonstick_spray or margarine?
		 */
		
		/*
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		IngredientRecipe grease = new IngredientRecipe("grease", false, false, false);
		grease.setOptions(true);
		grease.addToOptionsList(new IngredientRecipe("butter", false, false, false));
		grease.addToOptionsList(new IngredientRecipe("nonstick_spray", false, false, false));
		grease.addToOptionsList(new IngredientRecipe("margarine", false, false, false));
		ingredientList2.add(grease);
		IngredientRecipe ingredient3 = new IngredientRecipe("goal2", false, false, false, ingredientList3);
		*/
		
		this.topLevelIngredient = new IngredientRecipe("MoltenLavaCake", false, false, false, ingredientList2);
	}
}
