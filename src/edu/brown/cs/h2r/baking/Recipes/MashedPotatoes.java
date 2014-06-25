package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class MashedPotatoes extends Recipe {

	public MashedPotatoes() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe potatoes = knowledgebase.getIngredient("potatoes");
		potatoes.setPeeled();
		ingredientList.add(potatoes);
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe mashed_potatoes = new IngredientRecipe("Mashed_potatoes", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		mashed_potatoes.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		this.topLevelIngredient = mashed_potatoes;
	}

	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Mashed Potatoes",
		"Bring a large pot of salted water to a boil.\n",										//0
		"Peel potatoes and add to pot.  Cook until tender and drain.\n",							//1
		"Let cool and mash.\n",																	//2
		"Combine mashed potato, butter and egg in a large bowl.\n");								//3
			//8
	}
}
