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
		IngredientRecipe water = knowledgebase.getIngredient("water");
		saltedWaterList.add(water);
		water.setHeated();
		IngredientRecipe saltedWater = 
				new IngredientRecipe("salted_water", Recipe.HEATED, Recipe.SWAPPED, saltedWaterList);
		saltedWater.addNecessaryTrait("salt", Recipe.HEATED);
		
		
		
		List<IngredientRecipe> cookedPotatoesList = new ArrayList<IngredientRecipe>();
		IngredientRecipe potatoes = knowledgebase.getIngredient("potatoes");
		potatoes.addToolAttribute("peeled");
		potatoes.setHeated();
		cookedPotatoesList.add(potatoes);
		cookedPotatoesList.add(saltedWater);
		IngredientRecipe cookedPotatoes = new IngredientRecipe("potatoes_in_water", Recipe.HEATED, Recipe.SWAPPED, cookedPotatoesList);
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(cookedPotatoes);
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe mashedPotatoes = new IngredientRecipe("Mashed_potatoes", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		this.topLevelIngredient = mashedPotatoes;
	}

	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Mashed Potatoes",
		"Bring a large pot of salted water to a boil, then mix the pot to ensure evenly distributed salt.\n",										//0
		"Peel potatoes, add them to pot and stir the pot.  Cook until tender and drain.\n",							//1
		"Remove the potatoes from the heat. Combine potatoes, butter and egg in a large bowl, and mix until desired consistency has been reached.\n");								//2
	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		
		BakingPropositionalFunction saltedWaterPf = 
				new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("salted_water"));
		BakingSubgoal saltedWaterSubgoal = new BakingSubgoal(saltedWaterPf, swappedIngredients.get("salted_water"));
		this.subgoals.add(saltedWaterSubgoal);
		
		BakingPropositionalFunction cookedPotatoesPF = 
				new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("potatoes_in_water"));
		BakingSubgoal cookedPotatoesSubgoal = new BakingSubgoal(cookedPotatoesPF, swappedIngredients.get("potatoes_in_water"));
		cookedPotatoesSubgoal.addPrecondition(saltedWaterSubgoal);
		this.subgoals.add(cookedPotatoesSubgoal);
		
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		sg1.addPrecondition(cookedPotatoesSubgoal);
		
		this.subgoals.add(sg1);
	}
}
