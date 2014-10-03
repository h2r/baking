package edu.brown.cs.h2r.baking.Recipes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainerGreased;
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainersCleaned;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;


public class CherryBlondies extends Recipe { 
	public CherryBlondies() {
		super();
		this.recipeName = "cherry blondies";
		/**
		 *  Preheat oven to 325 degrees F (175 degrees C).
		 *	Beat butter, sugar, eggs and almond extract
		 *  Mix flour, salt, baking powder
		 *	Add drys to creamed ingredients
		 *	Add cherries, white chocolate and almonds
		 *	Bake in preheated oven for 35 to 40 minutes. Do not overcook.
		 */
				
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("brown_sugar"));
		ingredientList.add(knowledgebase.getIngredient("almond_extract"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe creamedIngredients = new IngredientRecipe("creamed_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(creamedIngredients.getName(), creamedIngredients);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("baking_powder"));
		IngredientRecipe dryIngs = new IngredientRecipe ("dry_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);
		dryIngs.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		dryIngs.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dryIngs.getName(), dryIngs);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(dryIngs);
		ingredientList4.add(creamedIngredients);
		ingredientList4.add(knowledgebase.getIngredient("dried_cherries"));
		ingredientList4.add(knowledgebase.getIngredient("white_chocolate"));
		ingredientList4.add(knowledgebase.getIngredient("sliced_almonds"));
		IngredientRecipe blondies = new IngredientRecipe("cherry_blondies", Recipe.BAKED, Recipe.SWAPPED, ingredientList4);
		this.topLevelIngredient = blondies;
		this.subgoalIngredients.put(blondies.getName(), blondies);
	}
	
	
	public void setUpSubgoals(Domain domain) {		
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("creamed_ingredients"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("creamed_ingredients"));
		BakingPropositionalFunction pf2clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("creamed_ingredients") );
		BakingSubgoal sg2clean = new BakingSubgoal(pf2clean, this.subgoalIngredients.get("creamed_ingredients"));
		this.subgoals.add(sg2);
		this.subgoals.add(sg2clean);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dry_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("dry_ingredients"));
		BakingPropositionalFunction pf3clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("dry_ingredients") );
		BakingSubgoal sg3clean = new BakingSubgoal(pf3clean, this.subgoalIngredients.get("dry_ingredients"));
		this.subgoals.add(sg3);
		this.subgoals.add(sg3clean);
		
				
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("cherry_blondies"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("cherry_blondies"));
		
		BakingPropositionalFunction pf4clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("cherry_blondies") );
		BakingSubgoal sg4clean = new BakingSubgoal(pf4clean, this.subgoalIngredients.get("cherry_blondies"));
		this.subgoals.add(sg4);
		this.subgoals.add(sg4clean);
	
		sg4.addPrecondition(sg3);
		sg4.addPrecondition(sg2);
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Chocolate chip cookies",
				 "Preheat oven to 325 degrees F (175 degrees C)",
				 "Beat sugar, eggs, almond extract and butter",
				 "Mix flour, salt and baking powder",
				 "Add dry ingredients to creamed ingredients",
				 "Add cherries, almonds and white chocolate",
				 "Bake in preheated oven for 35 to 40 minutes. Do not overcook.");		
	}
}