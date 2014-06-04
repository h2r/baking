package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
//import edu.brown.cs.h2r.baking.Parser;
//import java.util.HashMap;

import java.util.Set;
import java.util.TreeSet;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class Brownies extends Recipe {
	
	public Brownies() {
	super();
	//Parser parser = new Parser("");
	//HashMap<String,String> ingredientAttributes = parser.getAttributeMap();
	List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
	
	/*
	 * Make Ingredients
	 */
	IngredientRecipe baking_soda = new IngredientRecipe("baking_soda", false, false, false);
	baking_soda.addTraits(getTraits("baking_soda"));
	IngredientRecipe baking_powder = new IngredientRecipe("baking_powder", false, false, false);
	baking_powder.addTraits(getTraits("baking_powder"));
	IngredientRecipe flour = new IngredientRecipe("flour", false, false, false);
	flour.addTraits(getTraits("flour"));
	IngredientRecipe salt = new IngredientRecipe("salt", false, false, false);
	salt.addTraits(getTraits("salt"));
	
	// Add to compulsory Ingredient List
	ingredientList.add(baking_powder);
	ingredientList.add(baking_soda);
	
	// Add to compulsory traits
	Set<String> dry_traits = new HashSet<String>();
	dry_traits.add("flour");
	dry_traits.add("salt");
	
	// Make the subgoal and add all possible ingredients available.
	IngredientRecipe dry_ings = new IngredientRecipe ("dry_stuff", false, false, false, ingredientList, dry_traits);
	dry_ings.addTraits(getTraits("dry_stuff"));
	
	dry_ings.addPossibleIngredient(flour);
	dry_ings.addPossibleIngredient(salt);
	dry_ings.addPossibleIngredient(baking_powder);
	dry_ings.addPossibleIngredient(baking_soda);
	
	List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
	IngredientRecipe brown_sugar = new IngredientRecipe("brown_sugar", false, false, false);
	brown_sugar.addTraits(getTraits("brown_sugar"));
	IngredientRecipe white_sugar = new IngredientRecipe("white_sugar", false, false, false);
	white_sugar.addTraits(getTraits("white_sugar"));
	IngredientRecipe eggs= new IngredientRecipe("eggs", false, false, false);
	eggs.addTraits(getTraits("eggs"));
	IngredientRecipe cocoa = new IngredientRecipe("cocoa", false, false, false);
	cocoa.addTraits(getTraits("cocoa"));
	IngredientRecipe butter= new IngredientRecipe("butter", false, false, false);
	butter.addTraits(getTraits("butter"));
	
	ingredientList2.add(cocoa);
	
	Set<String> wet_traits = new HashSet<String>();
	wet_traits.add("eggs");
	wet_traits.add("sugar");
	wet_traits.add("fat");
	IngredientRecipe wet_ings = new IngredientRecipe("wet_stuff", false, false, false, ingredientList2, wet_traits);
	wet_ings.addTraits(getTraits("wet_stuff"));
	wet_ings.addPossibleIngredient(white_sugar);
	wet_ings.addPossibleIngredient(brown_sugar);
	wet_ings.addPossibleIngredient(cocoa);
	wet_ings.addPossibleIngredient(eggs);
	wet_ings.addPossibleIngredient(butter);


	List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
	ingredientList3.add(dry_ings);
	ingredientList3.add(wet_ings);
	IngredientRecipe brownies = new IngredientRecipe("brownies", false, false, false, ingredientList3);
	brownies.addTraits(getTraits("brownies"));
	brownies.addPossibleIngredient(wet_ings.getPossibleIngredients());
	brownies.addPossibleIngredient(dry_ings.getPossibleIngredients());
	
	this.topLevelIngredient = brownies;
	}
}
