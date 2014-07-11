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
			return this.checkPourIntoBakingContainer(s, pouringContainer);
		}
		
		if (ContainerFactory.isHeatingContainer(receivingContainer)) {
			return this.checkPourIntoHeatingContainer(s, pouringContainer);
		}
		
		
		if (ContainerFactory.isMixingContainer(pouringContainer) && 
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
			for (IngredientRecipe ing : necessaryIngs) {
				if (ing.getName().equals(content.getName())) {
					if (ing.AttributesMatch(content)) {
						currentMatch = true;
						break;
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
								if (ContainerFactory.isMixingContainer(container) && !ContainerFactory.isEmptyContainer(container)) {
									Set<String> cNames = ContainerFactory.getConstituentSwappedContentNames(container, s);
									for (String name : cNames) {
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
							currentMatch = necessaryTraits.get(trait).AttributesMatch(content);
							currentMatch = true;
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
	
	private boolean checkPourIntoBakingContainer(State s, ObjectInstance pouringContainer) {
		Set<String> contentNames = ContainerFactory.getContentNames(pouringContainer);
		for (String name :contentNames) {
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
		}
		return false;
		
	}
	
	private boolean checkPourIntoHeatingContainer(State s, ObjectInstance pouringContainer) {
		Set<String> contentNames = ContainerFactory.getContentNames(pouringContainer);
		for (String name : contentNames) {
			if (IngredientFactory.isMeltedIngredient(s.getObject(name))) {
				return false;
			}
			if (this.topLevelIngredient.getName().equals(name)) {
				return topLevelIngredient.getMelted();
			}
			List<IngredientRecipe> contents = this.topLevelIngredient.getContents();
			for (IngredientRecipe content : contents) {
				if (content.getName().equals(name)) {
					return content.getMelted();
				}
			}
		}
		return false;
	}
}
