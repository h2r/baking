package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
	public boolean isTrue(State state, String[] params) {
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		
		if (ContainerFactory.isBakingContainer(receivingContainer)) {
			if (!this.checkPourIntoBakingContainer(state, pouringContainer, receivingContainer)) {
				return false;
			}
		}
		
		if (ContainerFactory.isHeatingContainer(receivingContainer)) {
			if (!this.checkPourIntoHeatingContainer(state, pouringContainer, receivingContainer)) {
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
		
		// Look to see what  ingredients we have already fulfilled in our bowl
		Set<ObjectInstance> receivingContents = new HashSet<ObjectInstance>();
		Set<String> contentNames = ContainerFactory.getConstituentSwappedContentNames(receivingContainer, state);
		for (String contentName : contentNames) {
			ObjectInstance obj = state.getObject(contentName);
			receivingContents.add(obj);
		}
		// Check that everything in our bowl is either a necessary ingredient or a trait
		// ingredient for our current subgoal.
		if (!AllowPouring.allIngredientsNecessary(necessaryIngs, receivingContents, necessaryTraits)) {
			return false;
		}
		
		// Now, lets see if our pouring container has ingredients that are actually needed;
		if (!this.pouringNecessaryIngredients(state, pouringContainer, receivingContainer, necessaryIngs, necessaryTraits)) {
			return false;
		}
		
		// Ingredient we are pouring and those in the bowl we're pouring into are all pertinent
		// to our current subgoal and therefore this pour action will get us closer to our goal!
		return true;
	}
	
	private boolean checkPourIntoBakingContainer(State state, ObjectInstance pouringContainer,
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
				!ContainerFactory.hasABakedContent(state, receivingContainer)) {
			for (String name : pouringContentNames) {
				if (!this.checkBakingIngredient(state.getObject(name))) {
					return false;
				}
			}
		}
		ObjectInstance space = state.getObject(ContainerFactory.getSpaceName(receivingContainer));
		boolean willBake = SpaceFactory.isSwitchable(space) && SpaceFactory.getOnOff(space);
		if (willBake) {
			for (String name : pouringContentNames) {
				if (!this.checkBakingIngredient(state.getObject(name))) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean checkPourIntoHeatingContainer(State state, ObjectInstance pouringContainer,
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
				if (!this.checkHeatingIngredient(state.getObject(name))) {
					return false;
				}
			}
		} else {
			if (!ContainerFactory.hasAHeatedContent(state, receivingContainer)) {
				for (String name : pouringContentNames) {
					if (!this.checkHeatingIngredient(state.getObject(name))) {
						return false;
					}
				}
			}
		}
		ObjectInstance space = state.getObject(ContainerFactory.getSpaceName(receivingContainer));
		boolean willHeat = SpaceFactory.isSwitchable(space) && SpaceFactory.getOnOff(space);
		if (willHeat) {
			for (String name : pouringContentNames) {
				if (!this.checkHeatingIngredient(state.getObject(name))) {
					return false;
				}
			}
		}
		return true;
	}
	
	// checks to see if an ingredient is baked in the recipe
	private boolean checkBakingIngredient(ObjectInstance object) {
		String name = object.getName();
		if (IngredientFactory.isSimple(object)) {
			return false;
		}
		if (this.topLevelIngredient.getName().equals(name)) {
			return topLevelIngredient.getBaked();
		}
		List<IngredientRecipe> contents = this.topLevelIngredient.getConstituentIngredients();
		for (IngredientRecipe ingredient : contents) {
			if (ingredient.getName().equals(name)) {
				return ingredient.getBaked();
			}
		}
		return false;
	}
	
	// Checks to see if an ingredient is heated for the recipe
	private boolean checkHeatingIngredient(ObjectInstance object) {
		String name = object.getName();
		if (IngredientFactory.isHeatedIngredient(object)) {
			return false;
		}
		if (this.topLevelIngredient.getName().equals(name)) {
			return topLevelIngredient.getHeated();
		}
		List<IngredientRecipe> contents = this.topLevelIngredient.getContents();
		for (IngredientRecipe ingredient : contents) {
			if (ingredient.getName().equals(name)) {
				return ingredient.getHeated();
			}
		}
		for (Entry<String, IngredientRecipe> entry : this.topLevelIngredient.getNecessaryTraits().entrySet()) {
			String trait = entry.getKey();
			if (IngredientFactory.getTraits(object).contains(trait)) {
				return entry.getValue().getHeated();
			}
		}
		return false;
	}
	
	private static boolean allIngredientsNecessary(List<IngredientRecipe> necessaryIngs, Set<ObjectInstance> 
		receivingContents, AbstractMap<String, IngredientRecipe> necessaryTraits) {
		for (ObjectInstance ingObject : receivingContents) {
			String contentName = ingObject.getName();
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
					if (IngredientFactory.getTraits(ingObject).contains(trait)) {
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
		return true;
	}
	
	private boolean pouringNecessaryIngredients(State state, ObjectInstance pouringContainer,
			ObjectInstance receivingContainer, List<IngredientRecipe> necessaryIngs,
			AbstractMap<String, IngredientRecipe> necessaryTraits) {
		// Get all of the ingredients in our pouring bowl.
		Set<ObjectInstance> pourContents = new HashSet<ObjectInstance>();
		Set<String> names = ContainerFactory.getConstituentSwappedContentNames(pouringContainer, state);
		for (String name : names) {
			pourContents.add(state.getObject(name));
		}
		//Do all our ingredients fulfill a necessaryIngredient or traitIngredient?
		for (ObjectInstance ingObject : pourContents) {
			if(!this.fulfillsRequiredIngredient(necessaryIngs, receivingContainer, ingObject)) {
				if (!this.fulfillsTraitIngredient(state, necessaryTraits, pouringContainer,
						receivingContainer, ingObject)) {
					return false;
				}
			}
		}		
		return true;
	}
	
	private boolean fulfillsRequiredIngredient(List<IngredientRecipe> necessaryIngs, 
			ObjectInstance receivingContainer, ObjectInstance ingObject) {
		if (this.topLevelIngredient.getName().equals(ingObject.getName())) {
			return true;
		}
		for (IngredientRecipe ing : necessaryIngs) {
			if (ing.getName().equals(ingObject.getName())) {
				if (ContainerFactory.isMixingContainer(receivingContainer)) {
					if (ing.AttributesMatch(ingObject)) {
						return true;
					}
				} else {
					if (ing.toolAttributesMatch(ingObject)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean fulfillsTraitIngredient(State state, AbstractMap<String, IngredientRecipe> necessaryTraits,
			ObjectInstance pouringContainer, ObjectInstance receivingContainer, ObjectInstance ingObject) {
		Set<String> traits = necessaryTraits.keySet();
		for (String trait : traits) {
			if (IngredientFactory.getTraits(ingObject).contains(trait)) {
				// Check that this trait ingredient hasn't been added to some other bowl
				boolean traitUsed = false;
				List<ObjectInstance> containers = state.getObjectsOfTrueClass(ContainerFactory.ClassName);
				for (ObjectInstance container : containers) {
					if (!ContainerFactory.isMixingContainer(pouringContainer)) {
						if (!container.getName().equals(pouringContainer.getName())) {
							if (ContainerFactory.isReceivingContainer(container) && !ContainerFactory.isEmptyContainer(container)) {	
								Set<String> cNames = ContainerFactory.getConstituentSwappedContentNames(container, state);
								for (String name : cNames) {
									// we laready have the ingredient for this subgoal, no need to keep going!
									if (this.topLevelIngredient.getName().equals(name)) {
										return false;
									}
									ObjectInstance obj = state.getObject(name);
									if (IngredientFactory.getTraits(obj).contains(trait)) {
										traitUsed = true;
										break;
									}
								}
								
							}
						}
					}
				}
				if (!traitUsed) {
					// If we're pouring into a mixing container, then we must be sure that we have
					// the correct tool attributes (peeled,...) andregular attributes (baked, heated...).
					// Conversely, if we're pouring into a heating/baking container, then we definitely
					// want the correct tool attributes, but not necessarily the correct regular attributes
					// since the ingredient might be about to be baked or heated.
					if (ContainerFactory.isMixingContainer(receivingContainer)) {
						return necessaryTraits.get(trait).AttributesMatch(ingObject);
					} else {
						return necessaryTraits.get(trait).toolAttributesMatch(ingObject);
					}
				}
			}
		}
		return false;
	}
}
