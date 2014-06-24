package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class CucumberSalad extends Recipe {
	
	public CucumberSalad() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();

		ingredientList.add(knowledgebase.getIngredient("red_onions"));
		ingredientList.add(knowledgebase.getIngredient("tomatoes"));
		ingredientList.add(knowledgebase.getIngredient("cucumbers"));
		IngredientRecipe salad = new IngredientRecipe("Salad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("pepper"));
		ingredientList2.add(knowledgebase.getIngredient("olive_oil"));
		IngredientRecipe dressing = new IngredientRecipe("dressing", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		dressing.addNecessaryTrait("lemon", Recipe.NO_ATTRIBUTES);
		dressing.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList3= new ArrayList<IngredientRecipe>();
		ingredientList3.add(salad);
		ingredientList3.add(dressing);
		this.topLevelIngredient = new IngredientRecipe("CucumberSalad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);
		
	}
}
