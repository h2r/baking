package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.actions.PeelAction;

public class AllowPeeling extends BakingPropositionalFunction {

	public AllowPeeling(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName} ,ingredient);
	}
	
	public boolean isTrue(State state, String[] params) {
		if (!params[1].equalsIgnoreCase("")) {
			ObjectInstance container = state.getObject(params[1]);
			Set<String> contents = ContainerFactory.getContentNames(container);
			if (contents.isEmpty()) {
				return false;
			}
			ObjectInstance toPeel = null;
			for (String name : contents) {
				toPeel = state.getObject(name);
				boolean match = false;
				// Is this a necessary ingredient in the recipe?
				List<IngredientRecipe> ingredientContents = this.topLevelIngredient.getConstituentIngredients();
				for (IngredientRecipe content: ingredientContents) {
					if (content.getName().equals(toPeel.getName())) {
						// If it is, then make sure it needs to be peeled in the first place
						if (!content.hasToolAttribute(PeelAction.PEELED)) {
							return false;
						}
						match = true;
						break;
					}
				}
				if (!match) {
					// could this potentially fulfill a trait in the recipe? 
					AbstractMap<String, IngredientRecipe> necessaryTraits = this.topLevelIngredient.getNecessaryTraits();
					Set<String> toPeelTraits = IngredientFactory.getTraits(toPeel);
					for (Entry<String, IngredientRecipe> entry : necessaryTraits.entrySet()) {
						String trait = entry.getKey();
						if (toPeelTraits.contains(trait)) {
							// If it could potentially fulfill a trait ingredient, then ensure that 
							// it has to be peeled!
							if (entry.getValue().hasToolAttribute(PeelAction.PEELED)) {
								match = true;
								break;
							}
						}
					}
					if (!match) {
						return false;
					}
				}
			}
			return true;
		} else {
			// If no specific ingredient has been given to check, then allow the peeling action
			// Iff there exists some ingredient or trait ingredient that is peeled
			List<IngredientRecipe> contents = topLevelIngredient.getConstituentIngredients();
			for (IngredientRecipe ing : contents) {
				if (ing.hasToolAttribute(PeelAction.PEELED)) {
					return true;
				}
			}
			AbstractMap<String, IngredientRecipe> traitMap = topLevelIngredient.getConstituentNecessaryTraits();
			for (IngredientRecipe ing : traitMap.values()) {
				if (ing.hasToolAttribute(PeelAction.PEELED)) {
					return true;
				}
			}
			return false;
		}
	}
}
