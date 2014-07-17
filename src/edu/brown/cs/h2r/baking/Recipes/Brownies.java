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
		
		IngredientRecipe butter = knowledgebase.getIngredient("butter");
		butter.setSwapped();
		butter.setHeated();
		//IngredientRecipe meltedFat = new IngredientRecipe("meltedFat", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, new ArrayList<IngredientRecipe>());
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("vanilla"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		//ingredientList.add(meltedFat);
		ingredientList.add(butter);
		
		IngredientRecipe wetIngs = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		wetIngs.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		//wetIngs.addNecessaryTrait("fat", Recipe.HEATED);
		
		// In a large saucepan, melt 1/2 cup butter.
		
		// Add to compulsory Ingredient List
		// Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("baking_powder"));
		ingredientList2.add(knowledgebase.getIngredient("cocoa"));
		
		
		// Make the subgoal
		IngredientRecipe dryIngs = new IngredientRecipe ("dry_ingredients", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		// Add the necessaryTraits and their respective attributes
		dryIngs.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		dryIngs.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(dryIngs);
		ingredientList3.add(wetIngs);
		IngredientRecipe brownies = new IngredientRecipe("brownie_batter", Recipe.BAKED, Recipe.SWAPPED, ingredientList3);
		this.topLevelIngredient = brownies;
	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, SpaceFactory.SPACE_OVEN);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.topLevelIngredient);
		this.subgoals.add(sg2);

		BakingPropositionalFunction meltedFatPF = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("butter"));
		BakingSubgoal meltedFatSG = new BakingSubgoal(meltedFatPF, swappedIngredients.get("butter"));
		this.subgoals.add(meltedFatSG);
		meltedFatSG.addPrecondition(sg1);
		meltedFatSG.addPrecondition(sg2);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("wet_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, swappedIngredients.get("wet_ingredients"));
		sg3.addPrecondition(meltedFatSG);
		this.subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("dry_ingredients"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, swappedIngredients.get("dry_ingredients"));
		sg4.addPrecondition(sg1);
		sg4.addPrecondition(sg2);
		this.subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("brownies"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, swappedIngredients.get("brownie_batter"));
		sg5.addPrecondition(sg1);
		sg5.addPrecondition(sg3);
		sg5.addPrecondition(sg4);
		this.subgoals.add(sg5);
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Brownies",
				"Preheat oven to 350 degrees F (175 degrees C).\n",						//0
				"Grease and flour an 8-inch square pan.\n",								//1
				"In a large saucepan, melt 1/2 cup butter. \n",
				"In a mixing bowl, combine sugar, eggs, melted butter and 1 teaspoon vanilla.\n",							//3
				"In another bowl, combine 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.\n",		//4
				"Pour the wet ingredients onto the dry ingredients and mix to combine.\n",										//5
				"Pour the batter on the pan and bake in preheated oven for 25 to 30 minutes. Do not overcook.");		
	}
}
