package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class DeviledEggs extends Recipe {

	public DeviledEggs() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("egg_yolks"));
		ingredientList.add(knowledgebase.getIngredient("pepper"));
		
		IngredientRecipe yolk_mix = new IngredientRecipe("yolk_mix", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList);
		yolk_mix.addNecessaryTrait("salt", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		yolk_mix.addNecessaryTrait("mustard", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(new IngredientRecipe("chopped_tarragon", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED));
		ingredientList2.add(new IngredientRecipe("sweet_gherkins", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED));
		ingredientList2.add(new IngredientRecipe("shallots", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED,Recipe.NOT_PEELED));
		ingredientList2.add(yolk_mix);
		IngredientRecipe finished_mix = new IngredientRecipe("finished_mix", NOT_MIXED, NOT_MELTED, NOT_BAKED, NOT_PEELED, Recipe.SWAPPED, ingredientList2);

		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(new IngredientRecipe("egg_whites", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED));
		ingredientList3.add(finished_mix);
		
		this.topLevelIngredient = new IngredientRecipe("DeviledEggs", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList3);

	}
}
