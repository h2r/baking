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


public class ChocolateChipCookies extends Recipe { 
	public ChocolateChipCookies() {
		super();
		this.recipeName = "chocolate chip cookies";
		/**
		 *  Preheat oven to 350 degrees F (175 degrees C).
		 *	Mix flour, salt and baking soda
		 *  Mix butter and sugars
		 *	Add vanilla and eggs to butter and sugar
		 *	Add flour mixture
		 *	Add chocolate chips
		 *	Bake in preheated oven for 8 to 10 minutes. Do not overcook.
		 */
				
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("brown_sugar"));
		ingredientList.add(knowledgebase.getIngredient("white_sugar"));
		IngredientRecipe creamedIngredients = new IngredientRecipe("creamed_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(creamedIngredients.getName(), creamedIngredients);
		
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("vanilla"));
		ingredientList2.add(knowledgebase.getIngredient("eggs"));
		ingredientList2.add(creamedIngredients);
		IngredientRecipe wetIngs = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);

		this.subgoalIngredients.put(wetIngs.getName(), wetIngs);
		
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("baking_soda"));
		IngredientRecipe dryIngs = new IngredientRecipe ("dry_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);
		dryIngs.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		dryIngs.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dryIngs.getName(), dryIngs);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(dryIngs);
		ingredientList4.add(wetIngs);
		ingredientList4.add(knowledgebase.getIngredient("chocolate_chips"));
		IngredientRecipe cookies = new IngredientRecipe("chocolate_chip_cookies", Recipe.BAKED, Recipe.SWAPPED, ingredientList4);
		this.topLevelIngredient = cookies;
		this.subgoalIngredients.put(cookies.getName(), cookies);
	}
	
	
	public void setUpSubgoals(Domain domain) {		
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("creamed_ingredients"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("creamed_ingredients"));
		
		BakingPropositionalFunction pf2clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("creamed_ingredients") );
		BakingSubgoal sg2clean = new BakingSubgoal(pf2clean, this.subgoalIngredients.get("creamed_ingredients"));
		this.subgoals.add(sg2);
		this.subgoals.add(sg2clean);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("wet_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("wet_ingredients"));
		sg3.addPrecondition(sg2);
		
		BakingPropositionalFunction pf3clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("wet_ingredients") );
		BakingSubgoal sg3clean = new BakingSubgoal(pf3clean, this.subgoalIngredients.get("wet_ingredients"));
		this.subgoals.add(sg3);
		this.subgoals.add(sg3clean);
		
				
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dry_ingredients"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("dry_ingredients"));
		
		BakingPropositionalFunction pf4clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("dry_ingredients") );
		BakingSubgoal sg4clean = new BakingSubgoal(pf4clean, this.subgoalIngredients.get("dry_ingredients"));
		this.subgoals.add(sg4);
		this.subgoals.add(sg4clean);
		
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("chocolate_chip_cookies"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, this.subgoalIngredients.get("chocolate_chip_cookies"));
		sg5.addPrecondition(sg3);
		sg5.addPrecondition(sg4);
		this.subgoals.add(sg5);
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Chocolate chip cookies",
				 "Preheat oven to 350 degrees F (175 degrees C)",
				 "Mix salt, flour and baking soda",
				 "Mix butter and sugars",
				 "Add vanilla and eggs to butter and sugar",
				 "Add flour mixture",
				 "Add chocolate chips",
				 "Bake in preheated oven for 8 to 10 minutes. Do not overcook.");		
	}
}
