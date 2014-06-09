package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class MashedPotatoes extends Recipe {
	
	public MashedPotatoes() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("salt"));
		ingredientList.add(knowledgebase.getIngredient("potatoes"));
		
		IngredientRecipe mashed_potatoes = new IngredientRecipe("mashed_potatoes", false, false, false, ingredientList);
		mashed_potatoes.addNecessaryTrait("fat", NOTMIXED, NOTMELTED, NOTBAKED);
		this.topLevelIngredient = mashed_potatoes;
	}
}
