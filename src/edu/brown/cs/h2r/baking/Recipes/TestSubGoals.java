package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;


public class TestSubGoals extends Recipe {
	
	public TestSubGoals(int numIngredients, int numIngredientsPerSubgoal) {
		super();
		int numSubGoals = numIngredients / numIngredientsPerSubgoal;
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		int index = 0;
		for (int i =0; i < numSubGoals; ++i) {
			
			List<IngredientRecipe> subGoalList = new ArrayList<IngredientRecipe>();
			for (int j = 0; j < numIngredientsPerSubgoal; ++j) {				
				subGoalList.add(new IngredientRecipe("s" + index++, false, false, false));
			}
			ingredientList.add(new IngredientRecipe("p" + i, false, false, false, subGoalList));
		}
		this.topLevelIngredient = new IngredientRecipe("TestSubgoals", false, false, false, ingredientList);
	}
}
