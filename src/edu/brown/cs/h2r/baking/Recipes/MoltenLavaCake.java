package edu.brown.cs.h2r.baking.Recipes;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainerGreased;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;

public class MoltenLavaCake extends Recipe {
	
	public MoltenLavaCake() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe butter = knowledgebase.getIngredient("butter");
		butter.setMelted();
		ingredientList.add(butter);
		IngredientRecipe chocolate = knowledgebase.getIngredient("chocolate_squares");
		chocolate.setMelted();
		ingredientList.add(chocolate);
		IngredientRecipe melted = new IngredientRecipe("melted_stuff", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(melted);
		IngredientRecipe batter = new IngredientRecipe("batter", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		batter.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		batter.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(batter);
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(knowledgebase.getIngredient("egg_yolks"));
		IngredientRecipe unflavored_batter = new IngredientRecipe("unflavored_batter", Recipe.NO_ATTRIBUTES,Recipe.SWAPPED, ingredientList3);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(unflavored_batter);
		ingredientList4.add(knowledgebase.getIngredient("vanilla"));
		ingredientList4.add(knowledgebase.getIngredient("orange_liqueur"));
		this.topLevelIngredient = new IngredientRecipe("molten_lava_cake", Recipe.BAKED, Recipe.SWAPPED, ingredientList4);

	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.topLevelIngredient);
		this.subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("melted_stuff"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, swappedIngredients.get("melted_stuff"));
		sg3.addPrecondition(sg1);
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("batter"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, swappedIngredients.get("batter"));
		sg4.addPrecondition(sg3);
		this.subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("unflavored_batter"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, swappedIngredients.get("unflavored_batter"));
		sg5.addPrecondition(sg4);
		this.subgoals.add(sg5);
		
		BakingPropositionalFunction pf6 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("molten_lava_cake"));
		BakingSubgoal sg6 = new BakingSubgoal(pf6, swappedIngredients.get("molten_lava_cake"));
		sg6.addPrecondition(sg5);
		this.subgoals.add(sg6);
	}
	
	/**
	 * Preheat oven to 425 degrees F.
	 * Grease 6 (6-ounce) custard cups. 
	 * Melt the chocolates and butter in the microwave, or in a double boiler.
	 * Add the flour and sugar to chocolate mixture. 
	 * Stir in the eggs and yolks until smooth. 
	 * Stir in the vanilla and orange liqueur.
	 */

}
