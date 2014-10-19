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
	public MoltenLavaCake() {
		super();
		this.recipeName = "molten lava cake";
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe butter = knowledgebase.getIngredient("butter");
		butter.setHeated();
		butter.setHeatedState("melted");
		ingredientList.add(butter);
		IngredientRecipe chocolate = knowledgebase.getIngredient("chocolate_squares");
		chocolate.setHeated();
		chocolate.setHeatedState("melted");
		ingredientList.add(chocolate);
		IngredientRecipe melted = new IngredientRecipe("melted_stuff", Recipe.HEATED, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(melted.getName(), melted);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(melted);
		IngredientRecipe batter = new IngredientRecipe("batter", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		batter.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		batter.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(batter.getName(), batter);
		
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(batter);
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(knowledgebase.getIngredient("egg_yolks"));
		IngredientRecipe unflavoredBatter = new IngredientRecipe("unflavored_batter", Recipe.NO_ATTRIBUTES,Recipe.SWAPPED, ingredientList3);
		this.subgoalIngredients.put(unflavoredBatter.getName(), unflavoredBatter);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(unflavoredBatter);
		ingredientList4.add(knowledgebase.getIngredient("vanilla"));
		ingredientList4.add(knowledgebase.getIngredient("orange_liqueur"));
		IngredientRecipe cake = new IngredientRecipe("molten_lava_cake", Recipe.BAKED, Recipe.SWAPPED, ingredientList4);
		this.topLevelIngredient = cake;
		this.subgoalIngredients.put(cake.getName(), cake);
	}
	
	public void setUpSubgoals(Domain domain) {
		BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, SpaceFactory.SPACE_OVEN);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		this.subgoals.add(sg1);
		
		BakingPropositionalFunction pf2 = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.topLevelIngredient);
		this.subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("melted_stuff"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("melted_stuff"));
		sg3.addPrecondition(sg1);
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("batter"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("batter"));
		sg4.addPrecondition(sg3);
		this.subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("unflavored_batter"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, this.subgoalIngredients.get("unflavored_batter"));
		sg5.addPrecondition(sg4);
		this.subgoals.add(sg5);
		
		BakingPropositionalFunction pf6 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("molten_lava_cake"));
		BakingSubgoal sg6 = new BakingSubgoal(pf6, this.subgoalIngredients.get("molten_lava_cake"));
		sg6.addPrecondition(sg5);
		this.subgoals.add(sg6);
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
