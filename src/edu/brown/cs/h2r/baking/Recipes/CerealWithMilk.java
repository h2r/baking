package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;

public class CerealWithMilk extends Recipe {
	private static CerealWithMilk singleton = null;
	public CerealWithMilk(Domain domain) {
		super(domain);
		this.recipeName = "cereal with milk";
	}

	@Override
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("cereal"));
		//ingredientList.add(knowledgebase.getIngredient("brown_sugar"));
		ingredientList.add(knowledgebase.getIngredient("milk"));
		IngredientRecipe cerealWithMilk = new IngredientRecipe("cereal_with_milk", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(cerealWithMilk.getSimpleName(), cerealWithMilk);
		return cerealWithMilk;
		
	}

	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("cereal_with_milk"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("cereal_with_milk"));
		subgoals.add(sg2);
		return subgoals;
		
	}
	
	public static CerealWithMilk getRecipe(Domain domain) {
		if (CerealWithMilk.singleton == null) {
			CerealWithMilk.singleton = new CerealWithMilk(domain);
		}
		return CerealWithMilk.singleton;
	}

}
