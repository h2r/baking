package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;

public class Tea extends Recipe {

	private static Tea singleton = null;
	public Tea(Domain domain) {
		super(domain);
		this.recipeName = "tea";
	}
	@Override
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe water = knowledgebase.getIngredient("water");
		ingredientList.add(water);
		//ingredientList.add(knowledgebase.getIngredient("brown_sugar"));
		ingredientList.add(knowledgebase.getIngredient("tea"));
		IngredientRecipe tea = new IngredientRecipe("tea", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(tea.getSimpleName(), tea);
		return tea;
		
	}

	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("tea"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("tea"));
		subgoals.add(sg2);
		return subgoals;
		
	}
	
	public static Tea getRecipe(Domain domain) {
		if (Tea.singleton == null) {
			Tea.singleton = new Tea(domain);
		}
		return Tea.singleton;
	}

}
