package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class Brownies extends Recipe {
	public Brownies() {
		super();
		/**
		 *  Preheat oven to 350 degrees F (175 degrees C).
		 *	Grease and flour an 8-inch square pan.
		 *	In a large saucepan, melt 1/2 cup butter.
		 *	Stir in sugar, eggs, and 1 teaspoon vanilla.
		 *	Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.
		 *	Spread batter into prepared pan.
		 *	Bake in preheated oven for 25 to 30 minutes. Do not overcook.
		 */
		
		// Stir in sugar, eggs, and 1 teaspoon vanilla.
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("vanilla"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		
		IngredientRecipe wet_ings = new IngredientRecipe("wet_ingredients", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList);
		wet_ings.addNecessaryTrait("sugar", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		wet_ings.addTraits(knowledgebase.getTraits("wet_ingredients"));
		
		// In a large saucepan, melt 1/2 cup butter.
		wet_ings.addNecessaryTrait("fat", Recipe.NOT_MIXED, Recipe.MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		
		// Add to compulsory Ingredient List
		// Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("baking_powder"));
		ingredientList2.add(knowledgebase.getIngredient("cocoa"));
		
		
		// Make the subgoal
		IngredientRecipe dry_ings = new IngredientRecipe ("dry_ingredients", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList2);
		// Add the necessaryTraits and their respective attributes
		dry_ings.addNecessaryTrait("flour", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		dry_ings.addNecessaryTrait("salt", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		dry_ings.addTraits(knowledgebase.getTraits("dry_ingredients"));
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(dry_ings);
		ingredientList3.add(wet_ings);
		IngredientRecipe brownies = new IngredientRecipe("brownies", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList3);
		brownies.addTraits(knowledgebase.getTraits("brownies"));
		
		this.topLevelIngredient = brownies;
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Brownies",
				"Preheat oven to 350 degrees F (175 degrees C).\n",						//0
				"Grease and flour an 8-inch square pan.\n",								//1
				"In a large saucepan, melt 1/2 cup butter.\n",							//2
				"Stir in sugar, eggs, and 1 teaspoon vanilla.\n",							//3
				"Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.\n",		//4
				"Spread batter into prepared pan.\n",										//5
				"Bake in preheated oven for 25 to 30 minutes. Do not overcook.");		
	}
}
