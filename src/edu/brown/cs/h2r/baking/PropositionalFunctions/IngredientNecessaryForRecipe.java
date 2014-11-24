package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;

public class IngredientNecessaryForRecipe extends BakingPropositionalFunction {

	private final Knowledgebase knowledgebase;
	public IngredientNecessaryForRecipe(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {}, ingredient);
		this.knowledgebase = Knowledgebase.getKnowledgebase(domain);
		
	}
	
	public BakingPropositionalFunction updatePF(Domain newDomain, IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return new IngredientNecessaryForRecipe(this.name, newDomain, ingredient);
	}

	@Override
	// Out of all the ingredients in the kitchen, will check if the recipe *might* require it.
	// Check if it's listed as a necessary ingredient in the recipe, or if it contains
	// a trait that's listed as  a necessary trait for some component of the recipe.
	public boolean isTrue(State state, String[] params) {
		if (!params[0].equalsIgnoreCase("")) {
			String name = params[0];
			List<IngredientRecipe> contents = this.topLevelIngredient.getConstituentIngredients();
			for (IngredientRecipe ingredient : contents) {
				if (ingredient.getFullName().equals(params[0])) {
					return true;
				}
			}
			Set<String> ingredientTraits = this.topLevelIngredient.getConstituentNecessaryTraits().keySet();
			Set<String> traits = knowledgebase.getTraits(name);
			for (String trait : traits) {
				if (ingredientTraits.contains(trait)) {
					return true;
				}
			}
		}
		return false;
	}
}
