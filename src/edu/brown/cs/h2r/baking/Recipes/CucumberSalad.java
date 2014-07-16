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

public class CucumberSalad extends Recipe {
	
	public CucumberSalad() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();

		ingredientList.add(knowledgebase.getIngredient("red_onions"));
		ingredientList.add(knowledgebase.getIngredient("tomatoes"));
		ingredientList.add(knowledgebase.getIngredient("cucumbers"));
		IngredientRecipe salad = new IngredientRecipe("Salad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("pepper"));
		ingredientList2.add(knowledgebase.getIngredient("olive_oil"));
		IngredientRecipe dressing = new IngredientRecipe("dressing", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		dressing.addNecessaryTrait("lemon", Recipe.NO_ATTRIBUTES);
		dressing.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList3= new ArrayList<IngredientRecipe>();
		ingredientList3.add(salad);
		ingredientList3.add(dressing);
		IngredientRecipe cucumberSalad = new IngredientRecipe("CucumberSalad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);
		this.topLevelIngredient = cucumberSalad;
	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("Salad"));
		BakingSubgoal sg1 = new BakingSubgoal(pf1, swappedIngredients.get("Salad"));
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("dressing"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, swappedIngredients.get("dressing"));
		this.subgoals.add(sg2);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("CucumberSalad"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, swappedIngredients.get("CucumberSalad"));
		sg3.addPrecondition(sg1);
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
	}
	
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Cucumber Salad",
				"In a large bowl,add the onions, cucumbers and tomatoes, toss to combine. \n",		//1
				"In a small bowl, whisk together the lemon juice, olive oil and salt and pepper, to taste\n",	//2
				"Pour over the salad and serve immediately.\n"		//3
				);		
	}
}
