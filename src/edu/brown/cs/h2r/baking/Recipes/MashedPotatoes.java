package edu.brown.cs.h2r.baking.Recipes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;


public class MashedPotatoes extends Recipe {

	public MashedPotatoes() {
		super();
		
		List<IngredientRecipe> saltedWaterList = new ArrayList<IngredientRecipe>();
		saltedWaterList.add(knowledgebase.getIngredient("water"));
		IngredientRecipe saltedWater = 
				new IngredientRecipe("salted_water", Recipe.MELTED, Recipe.SWAPPED, saltedWaterList);
		saltedWater.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe potatoes = knowledgebase.getIngredient("potatoes");
		potatoes.setPeeled();
		ingredientList.add(potatoes);
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		ingredientList.add(saltedWater);
		IngredientRecipe mashed_potatoes = new IngredientRecipe("Mashed_potatoes", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		
		
		this.topLevelIngredient = mashed_potatoes;
	}

	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Mashed Potatoes",
		"Bring a large pot of salted water to a boil.\n",										//0
		"Peel potatoes and add to pot.  Cook until tender and drain.\n",							//1
		"Let cool and mash.\n",																	//2
		"Combine mashed potato, butter and egg in a large bowl.\n");								//3
			//8
	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		
		BakingPropositionalFunction saltedWaterPf = 
				new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("salted_water"));
		BakingSubgoal saltedWaterSubgoal = new BakingSubgoal(saltedWaterPf, swappedIngredients.get("salted_water"));
		
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		sg1.addPrecondition(saltedWaterSubgoal);
		
		this.subgoals.add(sg1);
	}
}
