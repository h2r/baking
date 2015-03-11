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
//	List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
//	IngredientRecipe eggs = knowledgebase.getIngredient("eggs");
//	eggs.setHeated();
//	ingredientList.add(eggs);
//	
//	IngredientRecipe friedEgg = new IngredientRecipe("fried_egg", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList);
//	friedEgg.addNecessaryTrait("fat", Recipe.HEATED);
//	this.subgoalIngredients.put(friedEgg.getSimpleName(), friedEgg);
//	return friedEgg;
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe water = knowledgebase.getIngredient("water");
		//water.setHeated();
		ingredientList.add(water);
		
		IngredientRecipe tea_leaves = knowledgebase.getIngredient("tea");
		//tea_leaves.setHeated();
		ingredientList.add(tea_leaves);
		
		IngredientRecipe tea = new IngredientRecipe("hot_tea", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList);
		tea.setHeated();
		this.subgoalIngredients.put(tea.getSimpleName(), tea);
		return tea;
		
	}

	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		IngredientRecipe tea = this.subgoalIngredients.get("hot_tea");
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, tea);
		BakingSubgoal sg2 = new BakingSubgoal(pf2, tea);
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
