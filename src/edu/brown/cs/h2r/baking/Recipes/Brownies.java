package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;
//import edu.brown.cs.h2r.baking.Parser;
//import java.util.HashMap;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class Brownies extends Recipe {
	
	public Brownies() {
	super();
	//Parser parser = new Parser("");
	//HashMap<String,String> ingredientAttributes = parser.getAttributeMap();
	List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
	
	IngredientRecipe baking_soda = new IngredientRecipe("baking_soda", false, false, false);
	baking_soda.addTraits(getTraits("baking_soda"));
	IngredientRecipe flour = new IngredientRecipe("flour", false, false, false);
	flour.addTraits(getTraits("flour"));
	IngredientRecipe baking_powder = new IngredientRecipe("baking_powder", false, false, false);
	baking_powder.addTraits(getTraits("baking_powder"));
	IngredientRecipe salt = new IngredientRecipe("salt", false, false, false);
	salt.addTraits(getTraits("salt"));
	ingredientList.add(salt);
	ingredientList.add(baking_powder);
	ingredientList.add(flour);
	ingredientList.add(baking_soda);
	IngredientRecipe dry_ings = new IngredientRecipe ("dry_stuff", false, false, false, ingredientList);
	dry_ings.addTraits(getTraits("dry_stuff"));
	
<<<<<<< HEAD
	List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
	IngredientRecipe sugar = new IngredientRecipe("sugar", false, false, false);
	sugar.addTraits(getTraits("sugar"));
	IngredientRecipe eggs= new IngredientRecipe("eggs", false, false, false);
	eggs.addTraits(getTraits("eggs"));
	IngredientRecipe cocoa = new IngredientRecipe("cocoa", false, false, false);
	cocoa.addTraits(getTraits("cocoa"));
	IngredientRecipe butter= new IngredientRecipe("butter", false, false, false);
	butter.addTraits(getTraits("butter"));
	ingredientList2.add(sugar);
	ingredientList2.add(eggs);
	ingredientList2.add(cocoa);
	ingredientList2.add(butter);
	IngredientRecipe wet_ings = new IngredientRecipe("wet_stuff", false, false, false, ingredientList2);
	wet_ings.addTraits(getTraits("wet_stuff"));

	List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
	ingredientList3.add(dry_ings);
	ingredientList3.add(wet_ings);
	IngredientRecipe Brownies = new IngredientRecipe("brownies", false, false, false, ingredientList3);
	Brownies.addTraits(getTraits("brownies"));
	
	this.topLevelIngredient = Brownies;
=======
	this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList);
>>>>>>> FETCH_HEAD
	}
}
