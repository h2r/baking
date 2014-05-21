package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class MashedPotatoes extends Recipe {
	
	public MashedPotatoes() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("Potatoes", false, false, false));
		ingredientList.add(new IngredientRecipe("Salt", false, false, false));
		ingredientList.add(new IngredientRecipe("Butter", false, false, false));
		ingredientList.add(new IngredientRecipe("Eggs", false, false, false));
		//ingredientList.add(new IngredientRecipe("Butter", false, false, false));
		//ingredientList.add(new IngredientRecipe("Flour", false, false, false));
		//ingredientList.add(new IngredientRecipe("Sugar", false, false, false));
		//ingredientList.add(new IngredientRecipe("Salt", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList);
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
