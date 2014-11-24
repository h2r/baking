package edu.brown.cs.h2r.baking.Recipes;

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
	public CucumberSalad(Domain domain) {
		super(domain);
		this.recipeName = "cucumber salad";
	}
	
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();

		ingredientList.add(knowledgebase.getIngredient("red_onions"));
		ingredientList.add(knowledgebase.getIngredient("tomatoes"));
		ingredientList.add(knowledgebase.getIngredient("cucumbers"));
		IngredientRecipe salad = new IngredientRecipe("Salad", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(salad.getSimpleName(), salad);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("pepper"));
		ingredientList2.add(knowledgebase.getIngredient("olive_oil"));
		IngredientRecipe dressing = new IngredientRecipe("dressing", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList2);
		dressing.addNecessaryTrait("lemon", Recipe.NO_ATTRIBUTES);
		dressing.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dressing.getSimpleName(), dressing);
		
		List<IngredientRecipe> ingredientList3= new ArrayList<IngredientRecipe>();
		ingredientList3.add(salad);
		ingredientList3.add(dressing);
		IngredientRecipe cucumberSalad = new IngredientRecipe("CucumberSalad", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList3);
		this.subgoalIngredients.put(cucumberSalad.getSimpleName(), cucumberSalad);
		
		return cucumberSalad;
	}
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("Salad"));
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.subgoalIngredients.get("Salad"));
		subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dressing"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("dressing"));
		subgoals.add(sg2);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("CucumberSalad"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("CucumberSalad"));
		sg3 = sg3.addPrecondition(sg1);
		sg3 = sg3.addPrecondition(sg2);
		subgoals.add(sg3);
		
		return subgoals;
	}
	
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Cucumber Salad",
				"In a large bowl,add the onions, cucumbers and tomatoes, toss to combine. \n",		//1
				"In a small bowl, whisk together the lemon juice, olive oil and salt and pepper, to taste\n",	//2
				"Pour over the salad and serve immediately.\n"		//3
				);		
	}
}
