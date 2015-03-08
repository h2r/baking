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


public class CranberryWalnutCookies extends Recipe { 
	private static CranberryWalnutCookies singleton = null;
	
	private CranberryWalnutCookies(Domain domain) {
		super(domain);
		this.recipeName = "cranberry walnut cookies";
	}
	
	public static CranberryWalnutCookies getRecipe(Domain domain) {
		if (CranberryWalnutCookies.singleton == null) {
			CranberryWalnutCookies.singleton = new CranberryWalnutCookies(domain);
		}
		return CranberryWalnutCookies.singleton;
	}
	
	protected IngredientRecipe createTopLevelIngredient() {
		/**
		 *  Preheat oven to 350 degrees F (175 degrees C).
		 *	Mix flour, cinnamon, salt and baking soda
		 *  Mix butter and sugars
		 *	Add vanilla and eggs to butter and sugar
		 *	Add flour mixture
		 *	Add walnuts and cranberries
		 *	Bake in preheated oven for 8 to 10 minutes. Do not overcook.
		 */
				
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(knowledgebase.getIngredient("butter"));
		ingredientList.add(knowledgebase.getIngredient("brown_sugar"));
		ingredientList.add(knowledgebase.getIngredient("white_sugar"));
		IngredientRecipe creamedIngredients = new IngredientRecipe("creamed_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList);
		this.subgoalIngredients.put(creamedIngredients.getSimpleName(), creamedIngredients);
		
		
		List<IngredientRecipe> ingredientList2 = new ArrayList<IngredientRecipe>();
		ingredientList2.add(knowledgebase.getIngredient("vanilla"));
		ingredientList2.add(knowledgebase.getIngredient("eggs"));
		ingredientList2.add(creamedIngredients);
		IngredientRecipe wetIngs = new IngredientRecipe("wet_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList2);

		this.subgoalIngredients.put(wetIngs.getSimpleName(), wetIngs);
		
		
		List<IngredientRecipe> ingredientList3 = new ArrayList<IngredientRecipe>();
		ingredientList3.add(knowledgebase.getIngredient("baking_soda"));
		ingredientList3.add(knowledgebase.getIngredient("cinnamon"));
		IngredientRecipe dryIngs = new IngredientRecipe ("dry_ingredients", Recipe.NO_ATTRIBUTES, this, Recipe.SWAPPED, ingredientList3);
		dryIngs.addNecessaryTrait("flour", Recipe.NO_ATTRIBUTES);
		dryIngs.addNecessaryTrait("salt", Recipe.NO_ATTRIBUTES);
		this.subgoalIngredients.put(dryIngs.getSimpleName(), dryIngs);
		
		List<IngredientRecipe> ingredientList4 = new ArrayList<IngredientRecipe>();
		ingredientList4.add(dryIngs);
		ingredientList4.add(wetIngs);
		ingredientList4.add(knowledgebase.getIngredient("dried_cranberries"));
		ingredientList3.add(knowledgebase.getIngredient("chopped_walnuts"));
		IngredientRecipe cookies = new IngredientRecipe("cranberry_walnut_cookies", Recipe.BAKED, this, Recipe.SWAPPED, ingredientList4);
		
		this.subgoalIngredients.put(cookies.getSimpleName(), cookies);
		
		return cookies;
	}
	
	public List<BakingSubgoal> getSubgoals(Domain domain) {		
		List<BakingSubgoal> subgoals = new ArrayList<BakingSubgoal>();
		BakingPropositionalFunction pf2 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("creamed_ingredients"));
		BakingSubgoal sg2 = new BakingSubgoal(pf2, this.subgoalIngredients.get("creamed_ingredients"));
		
		
		//BakingPropositionalFunction pf2clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("creamed_ingredients") );
		//BakingSubgoal sg2clean = new BakingSubgoal(pf2clean, this.subgoalIngredients.get("creamed_ingredients"));
		subgoals.add(sg2);
		//subgoals.add(sg2clean);
		
		BakingPropositionalFunction pf3 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("wet_ingredients"));
		BakingSubgoal sg3 = new BakingSubgoal(pf3, this.subgoalIngredients.get("wet_ingredients"));
		sg3 = sg3.addPrecondition(sg2);
		
		//BakingPropositionalFunction pf3clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("wet_ingredients") );
		//BakingSubgoal sg3clean = new BakingSubgoal(pf3clean, this.subgoalIngredients.get("wet_ingredients"));
		subgoals.add(sg3);
		//subgoals.add(sg3clean);
		
		
				
		
		BakingPropositionalFunction pf4 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("dry_ingredients"));
		BakingSubgoal sg4 = new BakingSubgoal(pf4, this.subgoalIngredients.get("dry_ingredients"));
		
		
		//BakingPropositionalFunction pf4clean = new ContainersCleaned(AffordanceCreator.CONTAINERS_CLEANED_PF, domain,this.subgoalIngredients.get("dry_ingredients") );
		//BakingSubgoal sg4clean = new BakingSubgoal(pf4clean, this.subgoalIngredients.get("dry_ingredients"));
		subgoals.add(sg4);
		//subgoals.add(sg4clean);
		
		
		BakingPropositionalFunction pf5 = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, this.subgoalIngredients.get("cranberry_walnut_cookies"));
		BakingSubgoal sg5 = new BakingSubgoal(pf5, this.subgoalIngredients.get("cranberry_walnut_cookies"));
		sg5 = sg5.addPrecondition(sg3);
		sg5 = sg5.addPrecondition(sg4);
		subgoals.add(sg5);
		
		return subgoals;
	}
	
	@Override
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe: Chocolate chip cookies",
				 "Preheat oven to 350 degrees F (175 degrees C)",
				 "Mix salt, cinnamon, flour and baking soda",
				 "Mix butter and sugars",
				 "Add vanilla and eggs to butter and sugar",
				 "Add flour mixture",
				 "Add cranberries, walnuts",
				 "Bake in preheated oven for 8 to 10 minutes. Do not overcook.");		
	}
}
