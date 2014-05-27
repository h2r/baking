package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class PecanPieSubGoals extends Recipe {

	public PecanPieSubGoals() {
		/*
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("salt", false, false, false));
		ingredientList.add(new IngredientRecipe("brown_sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("butter", false, false, false));
		IngredientRecipe ingredient1 = new IngredientRecipe("goal1", false, true, false, ingredientList);

		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>(); 
		ingredientList2.add(new IngredientRecipe("pecans", false, false, false));
		ingredientList2.add(new IngredientRecipe("vanilla_extract", false, false, false));
		ingredientList2.add(new IngredientRecipe("bourbon", false, false, false));
		ingredientList2.add(ingredient1);
		IngredientRecipe ingredient2 = new IngredientRecipe("goal2", false, false, false, ingredientList2);
		*/
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		IngredientRecipe dough = new IngredientRecipe("crust", false, false, false);
		//dough.setOptions(true);
		
		/**
		 * Will the user choose to use the frozen pie crust, or will they choose to
		 * make their own pie crust?
		 */
		
		/*List<IngredientRecipe> dough_list = new ArrayList<IngredientRecipe>();
		dough_list.add(new IngredientRecipe("flour", false, false, false));
		dough_list.add(new IngredientRecipe("sugar", false, false, false));
		dough_list.add(new IngredientRecipe("salt", false, false, false));
		dough_list.add(new IngredientRecipe("butter", false, false, false));
		dough_list.add(new IngredientRecipe("egg", false, false, false));*/
		
		//IngredientRecipe handmade = new IngredientRecipe("handmade_crust", false, false, false, dough_list);
		//dough.addToOptionsList(new IngredientRecipe("frozen_crust", false, false, false));
		//dough.addToOptionsList(handmade);
		ingredientList3.add(dough);
		//IngredientRecipe ingredient3 = new IngredientRecipe("goal2", false, false, false, ingredientList3);
		
	
		//List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		//ingredientList4.add(ingredient);
		//ingredientList4.add(ingredient2);
		this.topLevelIngredient = new IngredientRecipe("PecanPie", false, false, false, ingredientList3);
	}
}
