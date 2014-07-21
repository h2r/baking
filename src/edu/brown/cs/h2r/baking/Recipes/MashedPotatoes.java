package edu.brown.cs.h2r.baking.Recipes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;


public class MashedPotatoes extends Recipe {

	public MashedPotatoes() {
		super();
		
		List<IngredientRecipe> saltedWaterList = new ArrayList<IngredientRecipe>();
		IngredientRecipe water = knowledgebase.getIngredient("water");
		saltedWaterList.add(water);
		water.setHeated();
		water.setHeatedState("boiled");
		IngredientRecipe saltedWater = 
				new IngredientRecipe("salted_water", Recipe.HEATED, Recipe.SWAPPED, saltedWaterList);
		saltedWater.addNecessaryTrait("salt", Recipe.HEATED, "boiled");
		saltedWater.setHeatedState("boiled");
		this.subgoalIngredients.put("salted_water", saltedWater);
		
		IngredientRecipe potatoes = knowledgebase.getIngredient("potatoes");
		potatoes.addToolAttribute("peeled");
		potatoes.setSwapped();
		potatoes.setHeated();
		potatoes.setHeatedState("boiled");
		this.subgoalIngredients.put("potatoes", potatoes);
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("eggs"));
		ingredientList.add(potatoes);
		IngredientRecipe mashedPotatoes = new IngredientRecipe("Mashed_potatoes", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		this.topLevelIngredient = mashedPotatoes;
		this.subgoalIngredients.put("Mashed_potatoes", mashedPotatoes);
	}

	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Mashed Potatoes",
		"Bring a large pot of salted water to a boil, then mix the pot to ensure evenly distributed salt.\n",										//0
		"Peel potatoes, add them to pot and stir the pot.  Cook until tender and drain.\n",							//1
		"Remove the potatoes from the heat. Combine potatoes, butter and egg in a large bowl, and mix until desired consistency has been reached.\n");								//2
	}
	
	public void setUpSubgoals(Domain domain) {
		BakingPropositionalFunction saltedWaterPf = 
				new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("salted_water"));
		BakingSubgoal saltedWaterSubgoal = new BakingSubgoal(saltedWaterPf, this.subgoalIngredients.get("salted_water"));
		this.subgoals.add(saltedWaterSubgoal);
		
		BakingPropositionalFunction boiledPotatoesPF = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("potatoes"));
		BakingSubgoal boiledPotatoesSG = new BakingSubgoal(boiledPotatoesPF, this.subgoalIngredients.get("potatoes"));
		boiledPotatoesSG.addPrecondition(saltedWaterSubgoal);
		this.subgoals.add(boiledPotatoesSG);
		
		BakingPropositionalFunction pf1 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.topLevelIngredient);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		sg1.addPrecondition(boiledPotatoesSG);
		this.subgoals.add(sg1);
	}
}
