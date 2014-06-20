package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class PecanPie extends Recipe {
	
	public PecanPie() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("flour"));
		IngredientRecipe dry_crust = new IngredientRecipe("dry_crust", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList);
		dry_crust.addNecessaryTrait("salt", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		dry_crust.addNecessaryTrait("sugar", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("butter"));
		ingredientList2.add(dry_crust);
		IngredientRecipe flaky_crust = new IngredientRecipe("flaky_crust", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList2);

		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(flaky_crust);
		IngredientRecipe pie_crust = new IngredientRecipe("pie_crust", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList3);

		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(knowledgebase.getIngredient("butter"));
		ingredientList4.add(knowledgebase.getIngredient("light_corn_syrup"));
		IngredientRecipe pie_mix = new IngredientRecipe("pie_mix", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList4);
		pie_mix.addNecessaryTrait("salt", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		pie_mix.addNecessaryTrait("sugar", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
		
		
		List<IngredientRecipe> ingredientList5 = new ArrayList<IngredientRecipe>();
		ingredientList5.add(knowledgebase.getIngredient("pecans"));
		ingredientList5.add(knowledgebase.getIngredient("bourbon"));
		ingredientList5.add(knowledgebase.getIngredient("vanilla"));
		ingredientList5.add(pie_mix);
		IngredientRecipe filling = new IngredientRecipe("filling", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, Recipe.SWAPPED, ingredientList5);
		
		List<IngredientRecipe> ingredientList6 = new ArrayList<IngredientRecipe>();
		ingredientList6.add(filling);
		ingredientList6.add(pie_crust);
		this.topLevelIngredient = new IngredientRecipe("PecanPie", Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, NOT_PEELED, Recipe.SWAPPED, ingredientList6);
	}

}
