package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;
//import edu.brown.cs.h2r.baking.Parser;
//import java.util.HashMap;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class Brownies extends Recipe {
	
	/*public Brownies() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("Cocoa", false, false, false));
		ingredientList.add(new IngredientRecipe("Baking_soda", false, false, false));
		ingredientList.add(new IngredientRecipe("Baking_powder", false, false, false));
		ingredientList.add(new IngredientRecipe("Eggs", false, false, false));
		ingredientList.add(new IngredientRecipe("Butter", false, false, false));
		ingredientList.add(new IngredientRecipe("Flour", false, false, false));
		ingredientList.add(new IngredientRecipe("Sugar", false, false, false));
		ingredientList.add(new IngredientRecipe("Salt", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList);
	}*/
	
	public Brownies() {
	super();
	//Parser parser = new Parser("");
	//HashMap<String,String> ingredientAttributes = parser.getAttributeMap();
	List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
	
	IngredientRecipe flour = new IngredientRecipe("flour", false, false, false);
	//flour.set_affordance("is_dry");
	//IngredientRecipe baking_soda = new IngredientRecipe("baking_soda", false, false, false);
	//baking_soda.set_attribute("is_dry");
	//IngredientRecipe baking_powder = new IngredientRecipe("baking_powder", false, false, false);
	//baking_powder.set_attribute("is_dry");
	IngredientRecipe sugar = new IngredientRecipe("sugar", false, false, false);
	//sugar.set_affordance("is_wet");
	//IngredientRecipe salt= new IngredientRecipe("salt", false, false, false);
	//salt.set_attribute("is_dry");
	//IngredientRecipe cocoa = new IngredientRecipe("cocoa", false, false, false);
	//cocoa.set_attribute("is_wet");
	//IngredientRecipe eggs= new IngredientRecipe("eggs", false, false, false);
	//eggs.set_attribute("is_wet");
	//IngredientRecipe butter = new IngredientRecipe("butter", false, false, false);
	//butter.set_attribute("is_wet");
	
	//ingredientList.add(baking_soda);
	ingredientList.add(flour);
	//ingredientList.add(baking_powder);
	ingredientList.add(sugar);
	//ingredientList.add(salt);
	//ingredientList.add(eggs);
	//ingredientList.add(cocoa);
	//ingredientList.add(butter);
	
	this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList);
	}
}
