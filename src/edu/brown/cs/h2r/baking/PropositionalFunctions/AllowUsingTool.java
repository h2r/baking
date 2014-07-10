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
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;

public class AllowUsingTool extends BakingPropositionalFunction {

	public AllowUsingTool(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ToolFactory.ClassName, ContainerFactory.ClassName} ,ingredient);
	}
	
	public boolean isTrue(State state, String[] params) {
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		Set<String> contents = ContainerFactory.getContentNames(container);
		if (!params[2].equalsIgnoreCase("")) {
			if (contents.isEmpty()) {
				return false;
			}
			ObjectInstance ingredient;
			for (String name : contents) {
				ingredient = state.getObject(name);
				boolean match = false;
				// Is this a necessary ingredient in the recipe?
				for (IngredientRecipe content : this.topLevelIngredient.getConstituentIngredients()) {
					if (content.getName().equals(ingredient.getName())) {
						// Check to see if it can be used by the tool
						if (!ToolFactory.toolCanBeUsed(tool, content)) {
							return false;
						}
						// If it is, then make sure it needs to have the tool used on it in the first place
						if (!ToolFactory.toolHasBeenUsed(tool, content)) {
							return false;
						}
						match = true;
						break;
					}
				}
				if (!match) {
					// could this potentially fulfill a trait in the recipe? 
					AbstractMap<String, IngredientRecipe> necessaryTraits = this.topLevelIngredient.getNecessaryTraits();
					Set<String> toMeltTraits = IngredientFactory.getTraits(ingredient);
					for (String trait : necessaryTraits.keySet()) {
						if (toMeltTraits.contains(trait)) {
							// If it could potentially fulfill a trait ingredient, then ensure that 
							// it has to be peeled!
							IngredientRecipe ing = necessaryTraits.get(trait);
							if (ToolFactory.toolCanBeUsed(tool, ing) && 
									ToolFactory.toolHasBeenUsed(tool, ing)) {
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
			// If no specific ingredient has been given to check, then allow use action
			// Iff there exists some ingredient or trait ingredient that hs the trait!
			for (IngredientRecipe content : topLevelIngredient.getConstituentIngredients()) {
				if (ToolFactory.toolHasBeenUsed(tool, content)) {
					return true;
				}
			}
			AbstractMap<String, IngredientRecipe> traitMap = topLevelIngredient.getConstituentNecessaryTraits();
			for (String trait : traitMap.keySet()) {
				if (ToolFactory.toolHasBeenUsed(tool, traitMap.get(trait))) {
					return true;
				}
			}
			return false;
		}
	}
}
