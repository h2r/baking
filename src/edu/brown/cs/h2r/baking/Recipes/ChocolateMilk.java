package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;

public class ChocolateMilk extends Recipe {

	private static ChocolateMilk singleton = null;
	public ChocolateMilk(Domain domain) {
		super(domain);
		this.recipeName = "coffee with milk";
	}
	@Override
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("cocoa"));
		ingredientList.add(knowledgebase.getIngredient("milk"));
		IngredientRecipe chocolateMilk = new IngredientRecipe("chocolate_milk", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(chocolateMilk.getSimpleName(), chocolateMilk);
		return chocolateMilk;
		
	}

	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("chocolate_milk"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("chocolate_milk"));
		subgoals.add(sg2);
		return subgoals;
		
	}
	
	public static ChocolateMilk getRecipe(Domain domain) {
		if (ChocolateMilk.singleton == null) {
			ChocolateMilk.singleton = new ChocolateMilk(domain);
		}
		return ChocolateMilk.singleton;
	}
}
