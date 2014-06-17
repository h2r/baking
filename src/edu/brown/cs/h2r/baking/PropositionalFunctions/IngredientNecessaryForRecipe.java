package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class IngredientNecessaryForRecipe extends BakingPropositionalFunction {

	public IngredientNecessaryForRecipe(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {}, ingredient);
	}

	@Override
	// Out of all the ingredients in the kitchen, will check if the recipe *might* require it.
	// Check if it's listed as a necessary ingredient in the recipe, or if it contains
	// a trait that's listed as  a necessary trait for some component of the recipe.
	
	//FAKE METHOD FOR NOW -- REAL ONE BELOW!
	public boolean isTrue(State s, String[] params) {
		if (!params[0].equalsIgnoreCase("")) {
			String name = params[0];
			for (IngredientRecipe ingredient : this.topLevelIngredient.getConstituentIngredients()) {
				if (ingredient.getName().equals(params[0])) {
					return true;
				}
			}
			IngredientKnowledgebase knowledgebase = new IngredientKnowledgebase();
			Set<String> ingredientTraits = this.topLevelIngredient.getConstituentNecessaryTraits().keySet();
			for (String trait : knowledgebase.getTraits(name)) {
				if (ingredientTraits.contains(trait)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	  public boolean isTrue(State s, String[] params) {
		if (!params[0].equalsIgnoreCase("")) {
			String name = params[0];
			for (IngredientRecipe ingredient : this.topLevelIngredient.getConstituentIngredients()) {
				if (ingredient.getName().equals(params[0])) {
					return true;
				}
			}
			ObjectInstance toCheck = s.getObject(name);
			Set<String> ingredientTraits = this.topLevelIngredient.getConstituentNecessaryTraits();
			for (String trait : IngredientFactory.getTraits(toCheck)) {
				if (ingredientTraits.contains(trait)) {
					return true;
				}
			}
		}
		return false;
	}*/
}
