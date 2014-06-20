package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class MoltenLavaCake extends Recipe {
	
	public MoltenLavaCake() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe butter = knowledgebase.getIngredient("butter");
		butter.setMelted();
		ingredientList.add(butter);
		IngredientRecipe chocolate = knowledgebase.getIngredient("chocolate_squares");
		chocolate.setMelted();
		ingredientList.add(chocolate);
		IngredientRecipe melted = new IngredientRecipe("melted_stuff", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED, SWAPPED, ingredientList);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(melted);
		IngredientRecipe batter = new IngredientRecipe("batter", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED,SWAPPED, ingredientList2);
		batter.addNecessaryTrait("flour", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED);
		batter.addNecessaryTrait("sugar", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED);
		
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(batter);
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(knowledgebase.getIngredient("egg_yolks"));
		IngredientRecipe unflavored_batter = new IngredientRecipe("unflavored_batter", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED,SWAPPED, ingredientList3);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(unflavored_batter);
		ingredientList4.add(knowledgebase.getIngredient("vanilla"));
		ingredientList4.add(knowledgebase.getIngredient("orange_liqueur"));
		this.topLevelIngredient = new IngredientRecipe("molten_lava_cake", NOTMIXED, NOTMELTED, NOTBAKED,NOTPEELED, SWAPPED, ingredientList4);

	}

}
