package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;

public class ScrambledEgg extends Recipe {
	private static ScrambledEgg singleton = null;
	
	private ScrambledEgg(Domain domain) {
		super(domain);
		this.recipeName = "scrambled egg";
	}

	@Override
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe eggs = knowledgebase.getIngredient("eggs");
		eggs.setMixed();
		IngredientRecipe cookedEggs = eggs.getCopyWithNewAttributes(Recipe.NO_ATTRIBUTES);
		ingredientList.add(cookedEggs);
		
		IngredientRecipe friedEgg = new IngredientRecipe("scrambled_egg", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList);
		friedEgg.addNecessaryTrait("fat", Recipe.HEATED);
		this.subgoalIngredients.put(friedEgg.getSimpleName(), friedEgg);
		return friedEgg;
	}

	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("scrambled_egg"));
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.subgoalIngredients.get("scrambled_egg"));
		subgoals.add(sg1);
		return subgoals;
	}
	
	public static ScrambledEgg getRecipe(Domain domain) {
		if (singleton == null) {
			ScrambledEgg.singleton = new ScrambledEgg(domain);
		}
		return ScrambledEgg.singleton;
	}

}
