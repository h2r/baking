package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.ContainersCleaned;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;


public class BannanaBread extends Recipe { 
	public BannanaBread(Domain domain) {
		super(domain);
		this.recipeName = "bannana bread";
	}
	
	protected IngredientRecipe createTopLevelIngredient() {
		/**
		 *  Preheat oven to 350 degrees F (175 degrees C).
		 *  Mix bannanas and butter
		 *	Add flour, cinnamon, salt and baking soda
		 *	Add vanilla and eggs
		 *	Bake in preheated oven for 55 to 65 minutes. Do not overcook.
		 */
		
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		IngredientRecipe butter = knowledgebase.getIngredient("butter");
		//butter.setHeated();
		//butter.setHeatedState("melted");
		ingredientList.add(butter);
		ingredientList.add(knowledgebase.getIngredient("bannanas"));
		IngredientRecipe mashedIngredients = new IngredientRecipe("mashed_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(mashedIngredients.getName(), mashedIngredients);
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("baking_soda"));
		ingredientList2.add(mashedIngredients);
		IngredientRecipe dryIngs = new IngredientRecipe ("dry_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList2);
		dryIngs.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		dryIngs.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dryIngs.getName(), dryIngs);
				
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("vanilla"));
		ingredientList3.add(knowledgebase.getIngredient("eggs"));
		ingredientList3.add(dryIngs);
		IngredientRecipe bannanaBread = new IngredientRecipe("bannana_bread", Recipe.BAKED, this, Recipe.SWAPPED, ingredientList3);
		bannanaBread.addNecessaryTrait("sugar", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(bannanaBread.getName(), bannanaBread);
		
		return bannanaBread;
	}
	
	@Override
	public List<BakingSubgoal> getSubgoals(Domain domain) {		
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("mashed_ingredients"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("mashed_ingredients"));
		
		BakingPropositionalFunction pf2clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("mashed_ingredients") );
		BakingSubgoal sg2clean = new BakingSubgoal(pf2clean, this.subgoalIngredients.get("mashed_ingredients"));
		subgoals.add(sg2);
		subgoals.add(sg2clean);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dry_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("dry_ingredients"));
		sg3 = sg3.addPrecondition(sg2);
		
		BakingPropositionalFunction pf3clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("dry_ingredients") );
		BakingSubgoal sg3clean = new BakingSubgoal(pf3clean, this.subgoalIngredients.get("dry_ingredients"));
		sg3 = sg3.addPrecondition(sg2);
		subgoals.add(sg3);
		subgoals.add(sg3clean);
		
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("bannana_bread"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("bannana_bread"));
		sg4 = sg4.addPrecondition(sg3);
		subgoals.add(sg4);
		
		return subgoals;
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Chocolate chip cookies",
				 "Preheat oven to 350 degrees F (175 degrees C)",
				 "Mix butter and bannnas",
				 "Add salt, flour and baking soda",
				 "Add vanilla and eggs to butter and sugar",
				 "Bake in preheated oven for 55 to 65 minutes. Do not overcook.");		
	}
}
