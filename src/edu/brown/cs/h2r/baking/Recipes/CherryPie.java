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
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainersCleaned;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.PropositionalFunctions.SpaceOn;

public class CherryPie extends Recipe {
	public CherryPie() {
		super();
		this.recipeName = "cherry pie";
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("flour"));
		IngredientRecipe dryCrust = new IngredientRecipe("dry_crust", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList);
		dryCrust.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		dryCrust.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dryCrust.getName(), dryCrust);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("butter"));
		ingredientList2.add(dryCrust);
		IngredientRecipe flakyCrust = new IngredientRecipe("flaky_crust", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList2);
		this.subgoalIngredients.put(flakyCrust.getName(), flakyCrust);

		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(flakyCrust);
		IngredientRecipe pieCrust = new IngredientRecipe("pie_crust", Recipe.BAKED, Recipe.SWAPPED, ingredientList3);
		this.subgoalIngredients.put(pieCrust.getName(), pieCrust);

		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(knowledgebase.getIngredient("frozen_cherries"));
		ingredientList4.add(knowledgebase.getIngredient("cornstarch"));
		ingredientList4.add(knowledgebase.getIngredient("almond_extract"));
		IngredientRecipe pieMix = new IngredientRecipe("pie_mix", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, ingredientList4);
		pieMix.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(pieMix.getName(), pieMix);
		
		List<IngredientRecipe> ingredientList7 = new ArrayList<IngredientRecipe>();
		ingredientList7.add(pieMix);
		ingredientList7.add(pieCrust);
		IngredientRecipe cherryPie = new IngredientRecipe("cherryPie", Recipe.BAKED, Recipe.SWAPPED, ingredientList7);
		this.topLevelIngredient = cherryPie;
		this.subgoalIngredients.put(cherryPie.getName(), cherryPie);
	}
	
	public void setUpSubgoals(Domain domain) {
		/*BakingPropositionalFunction pf1 = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, this.topLevelIngredient, SpaceFactory.SPACE_OVEN);
		BakingSubgoal sg1 = new BakingSubgoal(pf1, this.topLevelIngredient);
		this.subgoals.add(sg1);*/
		
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dry_crust"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("dry_crust"));
		BakingPropositionalFunction pf2clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("dry_crust") );
		BakingSubgoal sg2clean = new BakingSubgoal(pf2clean, this.subgoalIngredients.get("dry_crust"));
		this.subgoals.add(sg2clean);
		//sg2.addPrecondition(sg1);
		this.subgoals.add(sg2);

		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("flaky_crust"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("flaky_crust"));
		BakingPropositionalFunction pf3clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("flaky_crust") );
		BakingSubgoal sg3clean = new BakingSubgoal(pf3clean, this.subgoalIngredients.get("flaky_crust"));
		this.subgoals.add(sg3clean);
		sg3.addPrecondition(sg2);
		this.subgoals.add(sg3);
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("pie_crust"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("pie_crust"));
		BakingPropositionalFunction pf4clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("pie_crust") );
		BakingSubgoal sg4clean = new BakingSubgoal(pf4clean, this.subgoalIngredients.get("pie_crust"));
		this.subgoals.add(sg4clean);
		sg4.addPrecondition(sg3);
		this.subgoals.add(sg4);
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("pie_mix"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, this.subgoalIngredients.get("pie_mix"));
		BakingPropositionalFunction pf5clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("pie_mix") );
		BakingSubgoal sg5clean = new BakingSubgoal(pf5clean, this.subgoalIngredients.get("pie_mix"));
		this.subgoals.add(sg5clean);
		this.subgoals.add(sg5);
		
		BakingPropositionalFunction pf6 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("PecanPie"));
		BakingSubgoal sg6 = new BakingSubgoal(pf6, this.subgoalIngredients.get("cherryPie"));
		BakingPropositionalFunction pf6clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("cherryPie") );
		BakingSubgoal sg6clean = new BakingSubgoal(pf6clean, this.subgoalIngredients.get("cherryPie"));
		this.subgoals.add(sg6clean);
		sg6.addPrecondition(sg4);
		sg6.addPrecondition(sg5);
		this.subgoals.add(sg6);
	}
	
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Pecan Pie",
				"Preheat to 400 degrees F.\n",						//0
				"In a medium bowl, whisk together the flour, sugar, and salt.\n",								//1
				"Using your fingers, work the butter into the dry ingredients \n",							//2
				"Add the egg and stir the dough together with a fork or by hand in the bowl. \n",							//3
				"Bake on a  baking sheet on the center rack until the dough is set, about 20 minutes. \n",		//4
				"In medium saucepan, combine the cherries, sugar, cornstarch and almond extract.\n",//5
				"Add the mix to the pie dough",
				"Bake for 35 minutes");		//9
	}

}
