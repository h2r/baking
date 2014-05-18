package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class DeviledEggsSubGoals extends Recipe {

	public DeviledEggsSubGoals() {
		super();
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("egg_yolks", false, false, false));
		ingredientList.add(new IngredientRecipe("salt", false, false, false));
		ingredientList.add(new IngredientRecipe("pepper", false, false, false));
		ingredientList.add(new IngredientRecipe("mustard", false, false, false));
		IngredientRecipe ingredient1 = new IngredientRecipe("goal1", false, false, false, ingredientList);

		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(ingredient1);
		ingredientList2.add(new IngredientRecipe("tarragon", false, false, false));
		ingredientList2.add(new IngredientRecipe("pickles", false, false, false));
		ingredientList2.add(new IngredientRecipe("shallots", false, false, false));
		//IngredientRecipe ingredient2 = new IngredientRecipe("goal2", false, false, false, ingredientList2);

		
		//List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		//ingredientList3.add(new IngredientRecipe("egg_whites", false, false, false));
		//ingredientList3.add(ingredient2);
		
		this.topLevelIngredient = new IngredientRecipe("DeviledEggs", false, false, false, ingredientList2);
		
	}
}
