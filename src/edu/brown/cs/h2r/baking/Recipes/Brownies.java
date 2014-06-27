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
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainerGreased;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;


public class Brownies extends Recipe {
	public Brownies() {
		super();
		/**
		 *  Preheat oven to 350 degrees F (175 degrees C).
		 *	Grease and flour an 8-inch square pan.
		 *	In a large saucepan, melt 1/2 cup butter.
		 *	Stir in sugar, eggs, and 1 teaspoon vanilla.
		 *	Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.
		 *	Spread batter into prepared pan.
		 *	Bake in preheated oven for 25 to 30 minutes. Do not overcook.
		 */
		
		// Stir in sugar, eggs, and 1 teaspoon vanilla.
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("vanilla"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		//ingredientList.add(melted_fat);
		
		IngredientRecipe wet_ings = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		wet_ings.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		wet_ings.addNecessaryTrait("fat", Recipe.MELTED);
		//wet_ings.addTraits(knowledgebase.getTraits("wet_ingredients"));
		
		// In a large saucepan, melt 1/2 cup butter.
		
		// Add to compulsory Ingredient List
		// Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("baking_powder"));
		ingredientList2.add(knowledgebase.getIngredient("cocoa"));
		
		
		// Make the subgoal
		IngredientRecipe dry_ings = new IngredientRecipe ("dry_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		// Add the necessaryTraits and their respective attributes
		dry_ings.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		dry_ings.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		//dry_ings.addTraits(knowledgebase.getTraits("dry_ingredients"));
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(dry_ings);
		ingredientList3.add(wet_ings);
		/*IngredientRecipe raw_brownies = new IngredientRecipe("raw_brownies", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList3);
		//brownies.addTraits(knowledgebase.getTraits("brownies"));
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		//raw_brownies.setBaked();
		ingredientList4.add(raw_brownies);*/
		IngredientRecipe brownies = new IngredientRecipe("brownies", Recipe.BAKED, Recipe.SWAPPED, ingredientList3);
		
		
		this.topLevelIngredient = brownies;
	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.topLevelIngredient);
		this.subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("wet_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, swappedIngredients.get("wet_ingredients"));
		sg3.addPrecondition(sg1);
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("dry_ingredients"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, swappedIngredients.get("dry_ingredients"));
		sg4.addPrecondition(sg1);
		sg4.addPrecondition(sg2);
		this.subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("brownies"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, swappedIngredients.get("brownies"));
		sg5.addPrecondition(sg1);
		sg5.addPrecondition(sg3);
		sg5.addPrecondition(sg4);
		this.subgoals.add(sg5);
		
		/*BakingPropositionalFunction pf6 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("brownies"));
		BakingSubgoal sg6 = new BakingSubgoal(pf6, swappedIngredients.get("brownies"));
		sg6.addPrecondition(sg1);
		sg6.addPrecondition(sg5);
		this.subgoals.add(sg6);*/
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Brownies",
				"Preheat oven to 350 degrees F (175 degrees C).\n",						//0
				"Grease and flour an 8-inch square pan.\n",								//1
				"In a large saucepan, melt 1/2 cup butter.\n",							//2
				"Stir in sugar, eggs, and 1 teaspoon vanilla.\n",							//3
				"Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.\n",		//4
				"Spread batter into prepared pan.\n",										//5
				"Bake in preheated oven for 25 to 30 minutes. Do not overcook.");		
	}
}
