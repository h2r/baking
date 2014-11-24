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

public class DeviledEggs extends Recipe {
	public DeviledEggs(Domain domain) {
		super(domain);
		this.recipeName = "deviled eggs";
	}
	
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("egg_yolks"));
		ingredientList.add(knowledgebase.getIngredient("pepper"));
		
		IngredientRecipe yolkMix = new IngredientRecipe("yolk_mix", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		yolkMix.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		yolkMix.addNecessaryTrait("mustard", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(yolkMix.getSimpleName(), yolkMix);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("chopped_tarragon"));
		ingredientList2.add(knowledgebase.getIngredient("sweet_gherkins"));
		ingredientList2.add(knowledgebase.getIngredient("shallots"));
		ingredientList2.add(yolkMix);
		IngredientRecipe finishedMix = new IngredientRecipe("finished_mix", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList2);
		this.subgoalIngredients.put(finishedMix.getSimpleName(), finishedMix);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("egg_whites"));
		ingredientList3.add(finishedMix);
		
		IngredientRecipe deviledEggs = new IngredientRecipe("DeviledEggs", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList3);
		
		this.subgoalIngredients.put(deviledEggs.getSimpleName(), deviledEggs);
		
		return deviledEggs;
	}
	
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("yolk_mix"));
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.subgoalIngredients.get("yolk_mix"));
		subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("finished_mix"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("finished_mix"));
		sg2 = sg2.addPrecondition(sg1);
		subgoals.add(sg2);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("DeviledEggs"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("DeviledEggs"));
		sg3 = sg3.addPrecondition(sg2);
		subgoals.add(sg3);
		return subgoals;
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Deviled Eggs",
				"Using a fork, mash the yolks with salt, pepper, to taste, and Dijon until smooth.\n",						//0
				"Add the shallot, gherkins, brine and tarragon and stir to combine.\n",								//1
				"Pipe the yolk mixture into the egg white halves. \n"); //2		
	}
}
