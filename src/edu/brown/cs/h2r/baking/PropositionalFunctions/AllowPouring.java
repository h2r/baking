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
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
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
			if (!this.checkPourIntoBakingContainer(s, pouringContainer, receivingContainer)) {
				return false;
			}
		}
		
		if (ContainerFactory.isHeatingContainer(receivingContainer)) {
			if (!this.checkPourIntoHeatingContainer(s, pouringContainer, receivingContainer)) {
				return false;
			}
		}
		
		// Avoid useless pouring back and forth!
		if (ContainerFactory.isReceivingContainer(pouringContainer) && 
				ContainerFactory.isMixingContainer(receivingContainer) && ContainerFactory.isEmptyContainer(receivingContainer)) {
			return false;
		}
		
		// Get what our subgoal is looking for and make copies
		List<IngredientRecipe> necessaryIngs = new ArrayList<IngredientRecipe>(); 
		necessaryIngs.addAll(this.topLevelIngredient.getContents());
		
		AbstractMap<String, IngredientRecipe> necessaryTraits = new HashMap<String, IngredientRecipe>();
		necessaryTraits.putAll(this.topLevelIngredient.getNecessaryTraits());
		
		// Look to see what trait/necessary ingredients we have already fulfilled in our bowl
		Set<ObjectInstance> receivingContents = new HashSet<ObjectInstance>();
		Set<String> contentNames = ContainerFactory.getConstituentSwappedContentNames(receivingContainer, s);
		for (String contentName : contentNames) {
			ObjectInstance obj = s.getObject(contentName);
			receivingContents.add(obj);
		}
		// Check that everything in our bowl is either a necessary ingredient or a trait
		// ingredient for our current subgoal.
		for (ObjectInstance content : receivingContents) {
			String contentName = content.getName();
			IngredientRecipe match = null;
			for (IngredientRecipe ing : necessaryIngs) {
				if (ing.getName().equals(contentName)) {
					match = ing;
					break;
				}
			}
			if (match != null) {
				necessaryIngs.remove(match);
			} else {
				String foundTrait = null;
				Set<String> traits = necessaryTraits.keySet();
				for (String trait : traits) {
					if (IngredientFactory.getTraits(content).contains(trait)) {
						foundTrait = trait;
						break;
					}
				}
				if (foundTrait != null) {
					necessaryTraits.remove(foundTrait);
				} else {
					// We're trying to pour into a bowl that doesn't have "good" ingredients
					// By good I mean relevant, in relation to our current subgoal.
					return false;
				}
			}
		}
		// Get all of the ingredients in our pouring bowl.
		Set<ObjectInstance> pourContents = new HashSet<ObjectInstance>();
		Set<String> names = ContainerFactory.getConstituentSwappedContentNames(pouringContainer, s);
		for (String name : names) {
			pourContents.add(s.getObject(name));
		}
		// Now, lets see if our pouring container has ingredients that are actually needed;
		Boolean currentMatch;
		for (ObjectInstance content : pourContents) {
			currentMatch = false;
			if (this.topLevelIngredient.getName().equals(content.getName())) {
				currentMatch = true;
			}
			for (IngredientRecipe ing : necessaryIngs) {
				if (ing.getName().equals(content.getName())) {
					//if (ing.AttributesMatch(content)) {
					if (ContainerFactory.isMixingContainer(receivingContainer)) {
						if (ing.AttributesMatch(content)) {
							currentMatch = true;
							break;
						}
					} else {
						if (ing.toolAttributesMatch(content)) {
							currentMatch = true;
							break;
						}
					}
				}
			}
			if (!currentMatch) {
				Set<String> traits = necessaryTraits.keySet();
				for (String trait : traits) {
					if (IngredientFactory.getTraits(content).contains(trait)) {
						// Check that this trait ingredient hasn't been added to some other bowl
						boolean traitUsed = false;
						List<ObjectInstance> containers = s.getObjectsOfTrueClass(ContainerFactory.ClassName);
						for (ObjectInstance container : containers) {
							if (!ContainerFactory.isMixingContainer(pouringContainer)) {
								//if (ContainerFactory.isMixingContainer(container) && !ContainerFactory.isEmptyContainer(container)) {
								if (ContainerFactory.isReceivingContainer(container) && !ContainerFactory.isEmptyContainer(container)
										&& !container.getName().equals(pouringContainer.getName())) {	
									Set<String> cNames = ContainerFactory.getConstituentSwappedContentNames(container, s);
									for (String name : cNames) {
										// we laready have the ingredient for this subgoal, no need to keep going!
										if (this.topLevelIngredient.getName().equals(name)) {
											return false;
										}
										ObjectInstance obj = s.getObject(name);
										if (IngredientFactory.getTraits(obj).contains(trait)) {
											traitUsed = true;
											break;
										}
									}
									
								}
							}
						}
						if (!traitUsed) {
							if (ContainerFactory.isMixingContainer(receivingContainer)) {
								currentMatch = necessaryTraits.get(trait).AttributesMatch(content);
							} else {
								currentMatch = necessaryTraits.get(trait).toolAttributesMatch(content);
							//currentMatch = true;
							}
							break;
						}
					}
				}
				if (!currentMatch) {
					return false;
				}
			}
		}
		// Ingredient we are pouring and those in the bowl we're pouring into are all pertinent
		// to our current subgoal and therefore this pour action will get us closer to our goal!
		return true;
	}
	
	private boolean checkPourIntoBakingContainer(State s, ObjectInstance pouringContainer,
			ObjectInstance receivingContainer) {
		/**
		 * If the container is empty then we only want to add in ingredients that we must
		 * bake, as per the recipe.
		 * Conversely, if the container is not empty:
		 * a) If it contains an already baked ingredient, then we can assume that we don't need to
		 * put the container back in the oven, and therefore we can add any ingredients.
		 * b) If all of the ingredients are non-bakeed, then we must assume that this container is meant
		 * to go in the oven in the near future, and therefore we will only add any ingredients
		 * that the recipe calls for to be baked.
		 */
		Set<String> pouringContentNames = ContainerFactory.getContentNames(pouringContainer);
		if (ContainerFactory.isEmptyContainer(receivingContainer) || 
				!ContainerFactory.hasABakedContent(s, receivingContainer)) {
			for (String name : pouringContentNames) {
				if (!this.checkBakingIngredient(s, name)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean checkPourIntoHeatingContainer(State s, ObjectInstance pouringContainer,
			ObjectInstance receivingContainer) {

		/**
		 * If the container is empty then we only want to add in ingredients that we must
		 * heat, as per the recipe.
		 * Conversely, if the container is not empty:
		 * a) If it contains an already heated ingredient, then we can assume that we don't need to
		 * put the container back on the burner, and therefore we can add any ingredients.
		 * b) If all of the ingredients are non-heated, then we must assume that this container is meant
		 * to go on the heating surface in the near future, and therefore we will only add any ingredients
		 * that the recipe calls for to be heated.
		 */
		Set<String> pouringContentNames = ContainerFactory.getContentNames(pouringContainer);
		if (ContainerFactory.isEmptyContainer(receivingContainer)) {
			for (String name : pouringContentNames) {
				if (!this.checkHeatingIngredient(s, name)) {
					return false;
				}
			}
		} else {
			if (!ContainerFactory.hasAHeatedContent(s, receivingContainer)) {
				for (String name : pouringContentNames) {
					if (!this.checkHeatingIngredient(s, name)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	// checks to see if an ingredient is baked in the recipe
	private boolean checkBakingIngredient(State s, String name) {
		if (IngredientFactory.isSimple(s.getObject(name))) {
			return false;
		}
		if (this.topLevelIngredient.getName().equals(name)) {
			return topLevelIngredient.getBaked();
		}
		List<IngredientRecipe> contents = this.topLevelIngredient.getConstituentIngredients();
		for (IngredientRecipe content : contents) {
			if (content.getName().equals(name)) {
				return content.getBaked();
			}
		}
		return false;
	}
	
	// Checks to see if an ingredient is heated for the recipe
	private boolean checkHeatingIngredient(State s, String name) {
		if (IngredientFactory.isHeatedIngredient(s.getObject(name))) {
			return false;
		}
		if (this.topLevelIngredient.getName().equals(name)) {
			return topLevelIngredient.getHeated();
		}
		List<IngredientRecipe> contents = this.topLevelIngredient.getContents();
		for (IngredientRecipe content : contents) {
			if (content.getName().equals(name)) {
				return content.getHeated();
			}
		}
		return false;
	}
}
