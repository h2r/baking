package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class MoltenLavaCake extends Recipe {
	
	public MoltenLavaCake() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		
		ingredientList.add(new IngredientRecipe("nonstick_spray", false, false, false));
		ingredientList.add(new IngredientRecipe("margarine", false, false, false));
		ingredientList.add(new IngredientRecipe("butter", false, false, false));
		
		ingredientList.add(new IngredientRecipe("chocolate", false, false, false));
		ingredientList.add(new IngredientRecipe("flour", false, false, false));
		ingredientList.add(new IngredientRecipe("sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("eggs", false, false, false));
		ingredientList.add(new IngredientRecipe("egg_yolks", false, false, false));
		ingredientList.add(new IngredientRecipe("vanilla_extract", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("MoltenLavaCake", false, false, false, ingredientList);

	}

}
