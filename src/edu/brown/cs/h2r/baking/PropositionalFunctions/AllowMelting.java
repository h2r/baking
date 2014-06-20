package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AllowMelting extends BakingPropositionalFunction {
	
	public AllowMelting(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName}, ingredient);
	}
	@Override
	public boolean isTrue(State s, String[] params) {
		if (!params[1].equalsIgnoreCase("")) {
			ObjectInstance container = s.getObject(params[1]);
			// Melting only simple ingredients, and not trying to melt empty bowl!
			if (!ContainerFactory.getContentNames(container).isEmpty() && !ContainerFactory.isMixingContainer(container)) {
				ObjectInstance toMelt = null;
				for (String name : ContainerFactory.getContentNames(container)) {
					toMelt = s.getObject(name);
					break;
				}
				//if (IngredientFactory.isMeltedIngredient(toMelt)) {
					//return false;
				//}
				// Is this a necessary ingredient in the recipe?
				for (IngredientRecipe content : this.topLevelIngredient.getConstituentIngredients()) {
					if (content.getName().equals(toMelt.getName())) {
						// If it is, then make sure it needs to be melted in the first place
						return content.getMelted();
					}
				}
				// could this potentially fulfill a trait in the recipe?
				AbstractMap<String, IngredientRecipe> necessaryTraits = this.topLevelIngredient.getNecessaryTraits();
				Set<String> toMeltTraits = IngredientFactory.getTraits(toMelt);
				for (String trait : necessaryTraits.keySet()) {
					if (toMeltTraits.contains(trait)) {
						// If it could potentially fulfill a trait ingredient, then ensure that 
						// it has to be melted!
						if (necessaryTraits.get(trait).getMelted()) {
							return true;
						}
					}
				}
				return false;
			}
			return false;
		} else {
			// If no specific ingredient has been given to check, then allow the melting action
			// Iff there exists some ingredient or trait ingredient that is melted!
			for (IngredientRecipe content : topLevelIngredient.getConstituentIngredients()) {
				if (content.getMelted()) {
					return true;
				}
			}
			AbstractMap<String, IngredientRecipe> traitMap = topLevelIngredient.getConstituentNecessaryTraits();
			for (String trait : traitMap.keySet()) {
				if (traitMap.get(trait).getMelted()) {
					return true;
				}
			}
			return false;
		}
	}

}
