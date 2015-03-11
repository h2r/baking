package edu.brown.cs.h2r.baking.Recipes;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;

public class FriedEgg extends Recipe {
	private static FriedEgg singleton = null;
	
	private FriedEgg(Domain domain) {
		super(domain);
		this.recipeName = "fried egg";
	}

	@Override
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe eggs = knowledgebase.getIngredient("eggs");
		eggs.setHeated();
		ingredientList.add(eggs);
		
		IngredientRecipe friedEgg = new IngredientRecipe("fried_egg", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList);
		friedEgg.addNecessaryTrait("fat", Recipe.HEATED);
		this.subgoalIngredients.put(friedEgg.getSimpleName(), friedEgg);
		return friedEgg;
	}

	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("fried_egg"));
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.subgoalIngredients.get("fried_egg"));
		subgoals.add(sg1);
		return subgoals;
	}
	
	public static FriedEgg getRecipe(Domain domain) {
		if (singleton == null) {
			FriedEgg.singleton = new FriedEgg(domain);
		}
		return FriedEgg.singleton;
	}

}
