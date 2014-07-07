package edu.brown.cs.h2r.baking.Recipes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainerGreased;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;


public class PeanutButterCookies extends Recipe {
	
	public PeanutButterCookies() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("peanut_butter"));
		IngredientRecipe creamed = new IngredientRecipe("creamed_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		creamed.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(creamed);
		ingredientList2.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe wet_ingredients = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("baking_soda"));
		ingredientList3.add(knowledgebase.getIngredient("baking_powder"));
		IngredientRecipe dry_ingredients = new IngredientRecipe("dry_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);
		dry_ingredients.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		dry_ingredients.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(wet_ingredients);
		ingredientList4.add(dry_ingredients);
		
		IngredientRecipe cookies = new IngredientRecipe("peanutButterCookies", Recipe.BAKED, Recipe.SWAPPED, ingredientList4);
		this.topLevelIngredient = cookies;
	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, "oven");
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("creamed_ingredients"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, swappedIngredients.get("creamed_ingredients"));
		sg2.addPrecondition(sg1);
		this.subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("wet_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, swappedIngredients.get("wet_ingredients"));
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("dry_ingredients"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, swappedIngredients.get("dry_ingredients"));
		sg4.addPrecondition(sg1);
		this.subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("peanutButterCookies"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, swappedIngredients.get("peanutButterCookies"));
		sg5.addPrecondition(sg3);
		sg5.addPrecondition(sg4);
		this.subgoals.add(sg5);
	}
	
	/**
	 * 1. Preheat oven to 375 degrees.
	 * 2. Cream together butter, peanut butter and sugars. 
	 * 3. Beat in eggs. 
	 * 4. In a separate bowl, sift together flour, baking powder, baking soda, and salt. 
	 * 5. Stir into batter. Put batter in refrigerator for 1 hour.
	 * 6. Bake for 10 minutes.
	 * 
	 */
}
