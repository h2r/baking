package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AllowPouring extends BakingPropositionalFunction {

	public AllowPouring(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName, ContainerFactory.ClassName}, ingredient);
	}
	@Override
	public boolean isTrue(State s, String[] params) {
		ObjectInstance pouringContainer = s.getObject(params[1]);
		ObjectInstance receivingContainer = s.getObject(params[2]);
		
		if (ContainerFactory.isBakingContainer(receivingContainer)) {
			for (String name :ContainerFactory.getContentNames(pouringContainer)) {
				if (IngredientFactory.isSimple(s.getObject(name))) {
					return false;
				}
			}
		}
		
		// Get what our subgoal is looking for and make copies
		List<IngredientRecipe> necessary_ings = new ArrayList<IngredientRecipe>(); 
		for (IngredientRecipe ing : this.topLevelIngredient.getContents()) {
			necessary_ings.add(ing);
		}
		AbstractMap<String, IngredientRecipe> actual_ing_traits = this.topLevelIngredient.getNecessaryTraits();
		//AbstractMap<String, IngredientRecipe> actual_ing_traits = this.topLevelIngredient.getConstituentNecessaryTraits();
		AbstractMap<String, IngredientRecipe> necessary_traits = new HashMap<String, IngredientRecipe>();
		for (String name : actual_ing_traits.keySet()) {
			necessary_traits.put(name, actual_ing_traits.get(name));
		}
		
		// Look to see what trait/necessary ingredients we have already fulfilled in our bowl
		Set<ObjectInstance> receiving_contents = new HashSet<ObjectInstance>();
		for (String content_name : ContainerFactory.getConstituentSwappedContentNames(receivingContainer, s)) {
			ObjectInstance obj = s.getObject(content_name);
			receiving_contents.add(obj);
		}
		// Check that everything in our bowl is either a necessary ingredient or a trait
		// ingredient for our current subgoal.
		for (ObjectInstance content : receiving_contents) {
			String content_name = content.getName();
			IngredientRecipe match = null;
			for (IngredientRecipe ing : necessary_ings) {
				if (ing.getName().equals(content_name)) {
					match = ing;
					break;
				}
			}
			if (match != null) {
				necessary_ings.remove(match);
			} else {
				String found_trait = null;
				for (String trait : necessary_traits.keySet()) {
					if (IngredientFactory.getTraits(content).contains(trait)) {
						found_trait = trait;
						break;
					}
				}
				if (found_trait != null) {
					necessary_traits.remove(found_trait);
				} else {
					// We're trying to pour into a bowl that doesn't have "good" ingredients
					// By good I mean relevant, in relation to our current subgoal.
					return false;
				}
			}
		}
		// Get all of the ingredients in our pouring bowl.
		Set<ObjectInstance> pour_contents = new HashSet<ObjectInstance>();
		for (String name : ContainerFactory.getConstituentSwappedContentNames(pouringContainer, s)) {
			pour_contents.add(s.getObject(name));
		}
		// Now, lets see if our pouring container has ingredients that are actually needed;
		Boolean current_match;
		for (ObjectInstance content : pour_contents) {
			current_match = false;
			for (IngredientRecipe ing : necessary_ings) {
				if (ing.getName().equals(content.getName())) {
					if (ing.AttributesMatch(content)) {
						current_match = true;
						break;
					}
				}
			}
			if (!current_match) {
				for (String trait : necessary_traits.keySet()) {
					if (IngredientFactory.getTraits(content).contains(trait)) {
						// Check that this trait ingredient hasn't been added to some other bowl
						boolean trait_used = false;
						for (ObjectInstance container : s.getObjectsOfTrueClass(ContainerFactory.ClassName)) {
							if (!ContainerFactory.isMixingContainer(pouringContainer)) {
								if (ContainerFactory.isMixingContainer(container) && !ContainerFactory.isEmptyContainer(container)) {
									for (String name : ContainerFactory.getConstituentSwappedContentNames(container, s)) {
										ObjectInstance obj = s.getObject(name);
										if (IngredientFactory.getTraits(obj).contains(trait)) {
											trait_used = true;
											break;
										}
									}
									
								}
							}
						}
						if (!trait_used) {
							current_match = necessary_traits.get(trait).AttributesMatch(content);
							break;
						}
					}
				}
				if (!current_match) {
					return false;
				}
			}
		}
		// Ingredient we are pouring and those in the bowl we're pouring into are all pertinent
		// to our current subgoal and therefore this pour action will get us closer to our goal!
		return true;
	}
}
