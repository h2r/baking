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
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainerGreased;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;

public class MoltenLavaCake extends Recipe {
	public MoltenLavaCake(Domain domain) {
		super(domain);
		this.recipeName = "molten lava cake";
	}
	
	protected IngredientRecipe createTopLevelIngredient() {
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe butter = knowledgebase.getIngredient("butter");
		butter.setHeated();
		butter.setHeatedState("melted");
		ingredientList.add(butter);
		IngredientRecipe chocolate = knowledgebase.getIngredient("chocolate_squares");
		chocolate.setHeated();
		chocolate.setHeatedState("melted");
		ingredientList.add(chocolate);
		IngredientRecipe melted = new IngredientRecipe("melted_stuff", Recipe.HEATED, this, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(melted.getSimpleName(), melted);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(melted);
		IngredientRecipe batter = new IngredientRecipe("batter", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList2);
		batter.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		batter.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(batter.getSimpleName(), batter);
		
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(batter);
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(knowledgebase.getIngredient("egg_yolks"));
		IngredientRecipe unflavoredBatter = new IngredientRecipe("unflavored_batter", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList3);
		this.subgoalIngredients.put(unflavoredBatter.getSimpleName(), unflavoredBatter);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(unflavoredBatter);
		ingredientList4.add(knowledgebase.getIngredient("vanilla"));
		ingredientList4.add(knowledgebase.getIngredient("orange_liqueur"));
		IngredientRecipe cake = new IngredientRecipe("molten_lava_cake", Recipe.BAKED, this, Recipe.SWAPPED, ingredientList4);
		
		this.subgoalIngredients.put(cake.getSimpleName(), cake);
		
		return cake;
	}
	public List<BakingSubgoal> getSubgoals(Domain domain) {
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, SpaceFactory.SPACE_OVEN);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.topLevelIngredient);
		subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("melted_stuff"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("melted_stuff"));
		sg3 = sg3.addPrecondition(sg1);
		sg3 = sg3.addPrecondition(sg2);
		subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("batter"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("batter"));
		sg4 = sg4.addPrecondition(sg3);
		subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("unflavored_batter"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, this.subgoalIngredients.get("unflavored_batter"));
		sg5 = sg5.addPrecondition(sg4);
		subgoals.add(sg5);
		
		BakingPropositionalFunction pf6 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("molten_lava_cake"));
		BakingSubgoal sg6 = new BakingSubgoal(pf6, this.subgoalIngredients.get("molten_lava_cake"));
		sg6 = sg6.addPrecondition(sg5);
		subgoals.add(sg6);
		
		return subgoals;
	}
	
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Molten Lava Cake",
				"Preheat oven to 425 degrees F\n",						//0
				"Grease 6 (6-ounce) custard cups\n",								//1
				"In a large saucepan, Melt the chocolates and butter\n",							//2
				"Add the flour and sugar to chocolate mixture.\n",							//3
				"Stir in the eggs and yolks until smooth. \n",		//4
				"Stir in the vanilla and orange liqueur\n",										//5
				"Spread the batter evenly between the custard cups \n",
				"Bake in preheated oven for 25 to 30 minutes. Do not overcook.");		
	}

}
