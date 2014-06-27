package edu.brown.cs.h2r.baking.Recipes;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;

public class DeviledEggs extends Recipe {

	public DeviledEggs() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("egg_yolks"));
		ingredientList.add(knowledgebase.getIngredient("pepper"));
		
		IngredientRecipe yolk_mix = new IngredientRecipe("yolk_mix", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		yolk_mix.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		yolk_mix.addNecessaryTrait("mustard", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("chopped_tarragon"));
		ingredientList2.add(knowledgebase.getIngredient("sweet_gherkins"));
		ingredientList2.add(knowledgebase.getIngredient("shallots"));
		ingredientList2.add(yolk_mix);
		IngredientRecipe finished_mix = new IngredientRecipe("finished_mix", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);

		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("egg_whites"));
		ingredientList3.add(finished_mix);
		
		this.topLevelIngredient = new IngredientRecipe("DeviledEggs", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);

	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("yolk_mix"));
		BakingSubgoal sg1 = new BakingSubgoal(pf1, swappedIngredients.get("yolk_mix"));
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("finished_mix"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, swappedIngredients.get("finished_mix"));
		sg2.addPrecondition(sg1);
		this.subgoals.add(sg2);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("DeviledEggs"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, swappedIngredients.get("DeviledEggs"));
		sg3.addPrecondition(sg1);
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
	}
}