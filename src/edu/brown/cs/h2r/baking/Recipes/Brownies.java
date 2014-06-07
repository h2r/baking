package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class Brownies extends Recipe {
	public Brownies() {
		super();
		
		// Add to compulsory Ingredient List
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("baking_powder"));
		ingredientList.add(knowledgebase.getIngredient("cocoa"));
		
		// Add to compulsory traits
		Set<String> dry_traits = new HashSet<String>();
		dry_traits.add("flour");
		dry_traits.add("salt");
		
		// Make the subgoal
		IngredientRecipe dry_ings = new IngredientRecipe ("dry_stuff", NOTMIXED, NOTMELTED, NOTBAKED, ingredientList, dry_traits);
		dry_ings.addTraits(knowledgebase.getTraits("dry_stuff"));
		
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("vanilla"));
		
		Set<String> wet_traits = new HashSet<String>();
		wet_traits.add("eggs");
		wet_traits.add("sugar");
		wet_traits.add("fat");
		IngredientRecipe wet_ings = new IngredientRecipe("wet_stuff", NOTMIXED, NOTMELTED, NOTBAKED, ingredientList2, wet_traits);
		wet_ings.addTraits(knowledgebase.getTraits("wet_stuff"));
	
	
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(dry_ings);
		ingredientList3.add(wet_ings);
		IngredientRecipe brownies = new IngredientRecipe("brownies", false, false, false, ingredientList3);
		brownies.addTraits(knowledgebase.getTraits("brownies"));
		
		this.topLevelIngredient = brownies;
	}
}
