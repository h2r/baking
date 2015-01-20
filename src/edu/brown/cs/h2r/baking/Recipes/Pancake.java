package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;

public class Pancake extends Recipe {
	private static Pancake singleton = null;
	public Pancake(Domain domain) {
		super(domain);
		this.recipeName = "pancake";
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("milk"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe batter = new IngredientRecipe ("batter", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList);
		batter.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(batter.getSimpleName(), batter);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(batter);
		IngredientRecipe pancake = new IngredientRecipe("pancake", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList2);
		//this.subgoalIngredients.put(pancake.getSimpleName(), pancake);
		return batter;
	}

	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("batter"));
		BakingSubgoal sg1 = new BakingSubgoal(pf, this.subgoalIngredients.get("batter"));
		subgoals.add(sg1);
		
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("pancake"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("pancake"));
		//subgoals.add(sg2);
	
		return subgoals;
	}

	public static Pancake getRecipe(Domain domain) {
		if (Pancake.singleton == null) {
			Pancake.singleton = new Pancake(domain);
		}
		return Pancake.singleton;
	}

}
