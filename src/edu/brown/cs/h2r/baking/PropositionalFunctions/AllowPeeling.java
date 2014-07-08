package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

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
				for (IngredientRecipe content : this.topLevelIngredient.getConstituentIngredients()) {
					if (content.getName().equals(toPeel.getName())) {
						// If it is, then make sure it needs to be peeled in the first place
						if (!content.getPeeled()) {
							return false;
						}
						match = true;
						break;
					}
				}
				if (!match) {
					// could this potentially fulfill a trait in the recipe? 
					AbstractMap<String, IngredientRecipe> necessaryTraits = this.topLevelIngredient.getNecessaryTraits();
					Set<String> toMeltTraits = IngredientFactory.getTraits(toPeel);
					for (String trait : necessaryTraits.keySet()) {
						if (toMeltTraits.contains(trait)) {
							// If it could potentially fulfill a trait ingredient, then ensure that 
							// it has to be peeled!
							if (necessaryTraits.get(trait).getPeeled()) {
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
			// If no specific ingredient has been given to check, then allow the melting action
			// Iff there exists some ingredient or trait ingredient that is melted!
			for (IngredientRecipe content : topLevelIngredient.getConstituentIngredients()) {
				if (content.getPeeled()) {
					return true;
				}
			}
			AbstractMap<String, IngredientRecipe> traitMap = topLevelIngredient.getConstituentNecessaryTraits();
			for (String trait : traitMap.keySet()) {
				if (traitMap.get(trait).getPeeled()) {
					return true;
				}
			}
			return false;
		}
	}
}
