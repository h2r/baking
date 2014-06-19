package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;

public class DeviledEggs extends Recipe {

	public DeviledEggs() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("egg_yolks"));
		ingredientList.add(knowledgebase.getIngredient("pepper"));
		
		IngredientRecipe yolk_mix = new IngredientRecipe("yolk_mix", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED, SWAPPED, ingredientList);
		yolk_mix.addNecessaryTrait("salt", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED);
		yolk_mix.addNecessaryTrait("mustard", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(new IngredientRecipe("chopped_tarragon", NOTMIXED, NOTMELTED, NOTBAKED,NOTPEELED));
		ingredientList2.add(new IngredientRecipe("sweet_gherkins", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED));
		ingredientList2.add(new IngredientRecipe("shallots", NOTMIXED, NOTMELTED, NOTBAKED,NOTPEELED));
		ingredientList2.add(yolk_mix);
		IngredientRecipe finished_mix = new IngredientRecipe("finished_mix", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED, SWAPPED, ingredientList2);

		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(new IngredientRecipe("egg_whites", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED));
		ingredientList3.add(finished_mix);
		
		this.topLevelIngredient = new IngredientRecipe("DeviledEggs", NOTMIXED, NOTMELTED, NOTBAKED, NOTPEELED, SWAPPED, ingredientList3);

	}
}
