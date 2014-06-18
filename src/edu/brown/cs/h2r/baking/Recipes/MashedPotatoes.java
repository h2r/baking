package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class MashedPotatoes extends Recipe {

	public MashedPotatoes() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("potatoes"));
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe mashed_potatoes = new IngredientRecipe("Mashed_potatoes", false, false, false, ingredientList);
		mashed_potatoes.addNecessaryTrait("salt", NOTMIXED, NOTMELTED, NOTBAKED);
		this.topLevelIngredient = mashed_potatoes;
	}

	/*@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Mashed Potatoes",
		"Bring a large pot of salted water to a boil.\n",										//0
		"Peel potatoes and add to pot.  Cook until tender and drain.\n",							//1
		"Let cool and mash.\n",																	//2
		"Combine mashed potato, butter and egg in a large bowl.\n");								//3
			//8
	}*/
}