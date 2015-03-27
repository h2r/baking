package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;


public class Brownies extends Recipe { 
	private static Brownies singleton = null;
	private Brownies(Domain domain) {
		super(domain);
		this.recipeName = "brownies";
	}
	
	protected IngredientRecipe createTopLevelIngredient() {

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
		//butter.setSwapped();
		//butter.setHeated();
		//butter.setHeatedState("melted");
		this.subgoalIngredients.put(butter.getSimpleName(), butter);
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("vanilla"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		ingredientList.add(butter);
		
		IngredientRecipe wetIngs = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		//wetIngs.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(wetIngs.getSimpleName(), wetIngs);
		
		// In a large saucepan, melt 1/2 cup butter.
		
		// Add to compulsory Ingredient List
		// Beat in 1/3 cup cocoa, 1/2 cup flour, salt, and baking powder.
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("baking_powder"));
		ingredientList2.add(knowledgebase.getIngredient("cocoa"));
		
		
		// Make the subgoal
		IngredientRecipe dryIngs = new IngredientRecipe ("dry_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList2);
		// Add the necessaryTraits and their respective attributes
		dryIngs.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		dryIngs.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dryIngs.getSimpleName(), dryIngs);
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(dryIngs);
		ingredientList3.add(wetIngs);
		IngredientRecipe brownies = new IngredientRecipe("brownie_batter", Recipe.BAKED, this, Recipe.SWAPPED, ingredientList3);
		this.subgoalIngredients.put(brownies.getSimpleName(), brownies);
		return brownies;
	}
	
	public static Brownies getRecipe(Domain domain) {
		if (Brownies.singleton == null) {
			Brownies.singleton = new Brownies(domain);
		}
		return Brownies.singleton;
	}
	
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		/*BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, SpaceFactory.SPACE_OVEN);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		subgoals.add(sg1);*/
		
		//BakingPropositionalFunction pf2 = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, this.topLevelIngredient);
		//BakingSubgoal sg2 = new BakingSubgoal(pf2, this.topLevelIngredient);
		//this.subgoals.add(sg2);

		//BakingPropositionalFunction meltedFatPF = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("butter"));
		//BakingSubgoal meltedFatSG = new BakingSubgoal(meltedFatPF, this.subgoalIngredients.get("butter"));
		//this.subgoals.add(meltedFatSG);
		//meltedFatSG = meltedFatSG.addPrecondition(sg1);
		//meltedFatSG = meltedFatSG.addPrecondition(sg2);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("wet_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("wet_ingredients"));
		//sg3 = sg3.addPrecondition(meltedFatSG);

		subgoals.add(sg3);
		//this.subgoals.add(sg3clean);
		
				
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dry_ingredients"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("dry_ingredients"));
		//sg4 = sg4.addPrecondition(sg1);
		//sg4.addPrecondition(sg2);

		subgoals.add(sg4);
		//this.subgoals.add(sg4clean);
		
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("brownie_batter"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, this.subgoalIngredients.get("brownie_batter"));
		sg5 = sg5.addPrecondition(sg3);
		sg5 = sg5.addPrecondition(sg4);
		subgoals.add(sg5);
		
		return subgoals;
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
