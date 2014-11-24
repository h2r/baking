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
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;


public class PeanutButterCookies extends Recipe {
	private static PeanutButterCookies singleton = null;
	private PeanutButterCookies(Domain domain) {
		super(domain);
		this.recipeName = "peanut butter cookies";
	}
	
	protected IngredientRecipe createTopLevelIngredient() {
		//IngredientRecipe butter = knowledgebase.getIngredient("butter");
		//butter.setSwapped();
		//butter.setHeated();
		//butter.setHeatedState("melted");
		//this.subgoalIngredients.put(butter.getName(), butter);
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("peanut_butter"));
		IngredientRecipe creamed = new IngredientRecipe("creamed_ingredients",
				Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		//creamed.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(creamed.getSimpleName(), creamed);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(creamed);
		ingredientList2.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe wetIngredients = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList2);
		this.subgoalIngredients.put(wetIngredients.getSimpleName(), wetIngredients);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("baking_soda"));
		ingredientList3.add(knowledgebase.getIngredient("baking_powder"));
		IngredientRecipe dryIngredients = new IngredientRecipe("dry_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList3);
		//dryIngredients.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		//dryIngredients.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dryIngredients.getSimpleName(), dryIngredients);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(wetIngredients);
		ingredientList4.add(dryIngredients);
		
		IngredientRecipe cookies = new IngredientRecipe("peanutButterCookies", Recipe.BAKED, this, Recipe.SWAPPED, ingredientList4);
		
		this.subgoalIngredients.put(cookies.getSimpleName(), cookies);
		
		return cookies;
	}
	
	public static PeanutButterCookies getRecipe(Domain domain) {
		if (PeanutButterCookies.singleton == null) {
			PeanutButterCookies.singleton = new PeanutButterCookies(domain);
		}
		return PeanutButterCookies.singleton;
	}
	
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, "oven");
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		//subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("creamed_ingredients"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("creamed_ingredients"));
		//sg2 = sg2.addPrecondition(sg1);
		subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("wet_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("wet_ingredients"));
		sg3 = sg3.addPrecondition(sg2);
		subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dry_ingredients"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("dry_ingredients"));
		//sg4 = sg4.addPrecondition(sg1);
		subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("peanutButterCookies"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, this.subgoalIngredients.get("peanutButterCookies"));
		sg5 = sg5.addPrecondition(sg3);
		sg5 = sg5.addPrecondition(sg4);
		subgoals.add(sg5);
		return subgoals;
	}
	
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Peanut Butter Cookies",
				"Preheat oven to 375 degrees F\n",						//0
				"Cream together butter, peanut butter and sugars\n",								//1
				"Beat in eggs\n",							//2
				"In a separate bowl, sift together flour, baking powder, baking soda, and salt. \n",							//3
				"Stir into batter.\n",		//4
				"Bake for 10 minutes.\n");		//5
	}
}
