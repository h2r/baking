package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
//import edu.brown.cs.h2r.baking.Parser;
//import java.util.HashMap;

import java.util.Set;
import java.util.TreeSet;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.KitchenIngredients;
import edu.brown.cs.h2r.baking.TraitKnowledgebase;


public class Brownies extends Recipe {
	public Brownies() {
		super();
		KitchenIngredients ingredients = new KitchenIngredients();
		TraitKnowledgebase traits = new TraitKnowledgebase();
		
		// Add to compulsory Ingredient List
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(ingredients.getIngredient("baking_powder"));
		ingredientList.add(ingredients.getIngredient("baking_soda"));
		
		// Add to compulsory traits
		Set<String> dry_traits = new HashSet<String>();
		dry_traits.add("flour");
		dry_traits.add("salt");
		
		// Make the subgoal
		IngredientRecipe dry_ings = new IngredientRecipe ("dry_stuff", false, false, false, ingredientList, dry_traits);
		dry_ings.addTraits(traits.getTraits("dry_stuff"));
		
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(ingredients.getIngredient("cocoa"));
		
		Set<String> wet_traits = new HashSet<String>();
		wet_traits.add("eggs");
		wet_traits.add("sugar");
		wet_traits.add("fat");
		IngredientRecipe wet_ings = new IngredientRecipe("wet_stuff", false, false, false, ingredientList2, wet_traits);
		wet_ings.addTraits(traits.getTraits("wet_stuff"));
	
	
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(dry_ings);
		ingredientList3.add(wet_ings);
		IngredientRecipe brownies = new IngredientRecipe("brownies", false, false, false, ingredientList3);
		brownies.addTraits(traits.getTraits("brownies"));
		
		this.topLevelIngredient = brownies;
	}
}
