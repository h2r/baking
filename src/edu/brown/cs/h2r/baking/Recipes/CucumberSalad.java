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
		IngredientRecipe salad = new IngredientRecipe("Salad", NOTMIXED, NOTMELTED, NOTBAKED, SWAPPED, ingredientList);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("pepper"));
		ingredientList2.add(knowledgebase.getIngredient("olive_oil"));
		IngredientRecipe dressing = new IngredientRecipe("dressing", NOTMIXED, NOTMELTED, NOTBAKED, SWAPPED, ingredientList2);
		dressing.addNecessaryTrait("lemon", NOTMIXED, NOTMELTED, NOTBAKED);
		dressing.addNecessaryTrait("salt", NOTMIXED, NOTMELTED, NOTBAKED);
		
		List<IngredientRecipe> ingredientList3= new ArrayList<IngredientRecipe>();
		ingredientList3.add(salad);
		ingredientList3.add(dressing);
		this.topLevelIngredient = new IngredientRecipe("CucumberSalad", false, false, false, ingredientList3);
		
	}
}
