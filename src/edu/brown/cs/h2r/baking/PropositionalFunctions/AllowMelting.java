package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.actions.MeltAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AllowMelting extends BakingPropositionalFunction {
	
	public AllowMelting(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName}, ingredient);
	}
	@Override
	public boolean isTrue(State s, String[] params) {
		// TODO Auto-generated method stub
		if (!params[1].equalsIgnoreCase("")) {
			ObjectInstance container = s.getObject(params[1]);
			if (!ContainerFactory.getContentNames(container).isEmpty() && !ContainerFactory.isMixingContainer(container)) {
				ObjectInstance toMelt = null;
				for (String name : ContainerFactory.getContentNames(container)) {
					toMelt = s.getObject(name);
					break;
				}
				if (IngredientFactory.isMeltedIngredient(toMelt)) {
					return false;
				}
				// Is this a necessary ingredient in the recipe?
				for (IngredientRecipe content : this.topLevelIngredient.getContents()) {
					if (content.getName().equals(toMelt.getName())) {
						return content.getMelted();
					}
				}
				// could this potentially fulfill a trait in the recipe?
				AbstractMap<String, IngredientRecipe> necessaryTraits = this.topLevelIngredient.getNecessaryTraits();
				Set<String> toMeltTraits = toMelt.getAllRelationalTargets("traits");
				for (String trait : necessaryTraits.keySet()) {
					if (toMeltTraits.contains(trait)) {
						if (necessaryTraits.get(trait).getMelted()) {
							return true;
						}
					}
				}
				return false;
			}
			return false;
		} else {
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
