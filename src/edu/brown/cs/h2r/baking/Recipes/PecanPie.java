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

public class PecanPie extends Recipe {
	
	public PecanPie() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("flour"));
		IngredientRecipe dry_crust = new IngredientRecipe("dry_crust", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		dry_crust.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		dry_crust.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("butter"));
		ingredientList2.add(dry_crust);
		IngredientRecipe flaky_crust = new IngredientRecipe("flaky_crust", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);

		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(flaky_crust);
		IngredientRecipe pie_crust = new IngredientRecipe("pie_crust", Recipe.BAKED, Recipe.SWAPPED, ingredientList3);

		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(knowledgebase.getIngredient("butter"));
		ingredientList4.add(knowledgebase.getIngredient("light_corn_syrup"));
		IngredientRecipe pie_mix = new IngredientRecipe("pie_mix", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList4);
		pie_mix.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		pie_mix.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		
		
		List<IngredientRecipe> ingredientList5 = new ArrayList<IngredientRecipe>();
		ingredientList5.add(knowledgebase.getIngredient("pecans"));
		ingredientList5.add(knowledgebase.getIngredient("bourbon"));
		ingredientList5.add(knowledgebase.getIngredient("vanilla"));
		ingredientList5.add(pie_mix);
		IngredientRecipe filling = new IngredientRecipe("filling", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList5);
		

		List<IngredientRecipe> ingredientList6 = new ArrayList<IngredientRecipe>();
		ingredientList6.add(filling);
		ingredientList6.add(knowledgebase.getIngredient("eggs"));
		IngredientRecipe finished_filling = new IngredientRecipe("finished_filling", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList6);
		
		List<IngredientRecipe> ingredientList7 = new ArrayList<IngredientRecipe>();
		ingredientList7.add(finished_filling);
		ingredientList7.add(pie_crust);
		this.topLevelIngredient = new IngredientRecipe("PecanPie", Recipe.BAKED, Recipe.SWAPPED, ingredientList7);
	}
	
	public void setUpSubgoals(Domain domain) {
		AbstractMap<String, IngredientRecipe> swappedIngredients = IngredientRecipe.getRecursiveSwappedIngredients(this.topLevelIngredient);
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, "oven");
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("dry_crust"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, swappedIngredients.get("dry_crust"));
		sg2.addPrecondition(sg1);
		this.subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("flaky_crust"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, swappedIngredients.get("flaky_crust"));
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("pie_crust"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, swappedIngredients.get("pie_crust"));
		sg4.addPrecondition(sg3);
		this.subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("pie_mix"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, swappedIngredients.get("pie_mix"));
		sg5.addPrecondition(sg4);
		this.subgoals.add(sg5);
		
		BakingPropositionalFunction pf6 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("filling"));
		BakingSubgoal sg6 = new BakingSubgoal(pf6, swappedIngredients.get("filling"));
		sg6.addPrecondition(sg5);
		this.subgoals.add(sg6);
		
		BakingPropositionalFunction pf7 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("finished_filling"));
		BakingSubgoal sg7 = new BakingSubgoal(pf7, swappedIngredients.get("finished_filling"));
		sg7.addPrecondition(sg6);
		this.subgoals.add(sg7);
		
		BakingPropositionalFunction pf8 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, swappedIngredients.get("PecanPie"));
		BakingSubgoal sg8 = new BakingSubgoal(pf8, swappedIngredients.get("PecanPie"));
		sg8.addPrecondition(sg4);
		sg8.addPrecondition(sg7);
		this.subgoals.add(sg8);
	}
	
	/**
	 * 1. Preheat to 400 degrees F.
	 * 2. In a medium bowl, whisk together the flour, sugar, and salt. 
	 * 3. Using your fingers, work the butter into the dry ingredients 
	 * 4. Add the egg and stir the dough together with a fork or by hand in the bowl. 
	 * 5. Bake on a  baking sheet on the center rack until the dough is set, about 20 minutes. 
	 * 6. While the crust is baking make the filling: In medium saucepan, combine the butter, brown sugar, 
	 * 	  corn syrup, and salt.
	 * 7. Remove from the heat and stir in the nuts, bourbon, and the vanilla.
     * 8. Whisk the beaten eggs into the filling until smooth. 
     * 9. Put the pie shell on a sheet pan and pour the filling into the hot crust.
	 */

}
