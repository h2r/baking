package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class PecanPie extends Recipe {
	
	public PecanPie() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("flour"));
		IngredientRecipe dry_crust = new IngredientRecipe("dry_crust", NOTMIXED, NOTMELTED, NOTBAKED, SWAPPED, ingredientList);
		dry_crust.addNecessaryTrait("salt", NOTMIXED, NOTMELTED, NOTBAKED);
		dry_crust.addNecessaryTrait("sugar", NOTMIXED, NOTMELTED, NOTBAKED);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("butter"));
		ingredientList2.add(dry_crust);
		IngredientRecipe flaky_crust = new IngredientRecipe("flaky_crust", NOTMIXED, NOTMELTED, NOTBAKED, SWAPPED, ingredientList2);

		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(flaky_crust);
		IngredientRecipe pie_curst = new IngredientRecipe("pie_crust", NOTMIXED, NOTMELTED, NOTBAKED, SWAPPED, ingredientList3);

		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(knowledgebase.getIngredient("butter"));
		ingredientList4.add(knowledgebase.getIngredient("light_corn_syrup"));
		IngredientRecipe pie_mix = new IngredientRecipe("pie_mix", NOTMIXED, NOTMELTED, NOTBAKED, SWAPPED, ingredientList4);
		pie_mix.addNecessaryTrait("salt", NOTMIXED, NOTMELTED, NOTBAKED);
		pie_mix.addNecessaryTrait("sugar", NOTMIXED, NOTMELTED, NOTBAKED);
		
		
		List<IngredientRecipe> ingredientList5 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(knowledgebase.getIngredient("pecans"));
		ingredientList4.add(knowledgebase.getIngredient("bourbon"));
		ingredientList4.add(knowledgebase.getIngredient("vanilla"));
		ingredientList4.add(pie_mix);
		IngredientRecipe filling = new IngredientRecipe("filling", NOTMIXED, NOTMELTED, NOTBAKED, SWAPPED, ingredientList5);
		
		List<IngredientRecipe> ingredientList6 = new ArrayList<IngredientRecipe>();
		ingredientList6.add(filling);
		ingredientList6.add(flaky_crust);
		this.topLevelIngredient = new IngredientRecipe("PecanPie", false, false, false, ingredientList6);
	}

}
