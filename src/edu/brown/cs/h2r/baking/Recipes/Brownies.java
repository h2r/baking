package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

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
		
		IngredientRecipe wet_ings = new IngredientRecipe("wet_ingredients", NOTMIXED, NOTMELTED, NOTBAKED, ingredientList);
		wet_ings.setSwapped();
		wet_ings.addNecessaryTrait("eggs", NOTMIXED, NOTMELTED, NOTBAKED);
		wet_ings.addNecessaryTrait("sugar", NOTMIXED, NOTMELTED, NOTBAKED);
		wet_ings.addTraits(knowledgebase.getTraits("wet_ingredients"));
		
		// In a large saucepan, melt 1/2 cup butter.
		wet_ings.addNecessaryTrait("fat", NOTMIXED, MELTED, NOTBAKED);
		
		// Add to compulsory Ingredient List
		// Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("baking_powder"));
		ingredientList2.add(knowledgebase.getIngredient("cocoa"));
		
		
		// Make the subgoal
		IngredientRecipe dry_ings = new IngredientRecipe ("dry_ingredients", NOTMIXED, NOTMELTED, NOTBAKED, ingredientList2);
		dry_ings.setSwapped();
		// Add the necessaryTraits and their respective attributes
		dry_ings.addNecessaryTrait("flour", NOTMIXED, NOTMELTED, NOTBAKED);
		dry_ings.addNecessaryTrait("salt", NOTMIXED, NOTMELTED, NOTBAKED);
		dry_ings.addTraits(knowledgebase.getTraits("dry_ingredients"));
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(dry_ings);
		ingredientList3.add(wet_ings);
		IngredientRecipe brownies = new IngredientRecipe("brownies", NOTMIXED, NOTMELTED, NOTBAKED, ingredientList3);
		brownies.addTraits(knowledgebase.getTraits("brownies"));
		
		this.topLevelIngredient = brownies;
	}
}
