package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class AllowMoving extends BakingPropositionalFunction {

	public AllowMoving(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName}, ingredient) ;
	}
	
	public BakingPropositionalFunction updatePF(Domain newDomain, IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return new AllowMoving(this.name, newDomain, ingredient);
	}
	
	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance space = state.getObject(params[2]);
		ObjectInstance container = state.getObject(params[1]);		
		Set<String> contents = ContainerFactory.getContentNames(container);
		
		String containerSpaceName = ContainerFactory.getSpaceName(container);
		ObjectInstance containerSpace = state.getObject(containerSpaceName);
		
		//if (SpaceFactory.isCleaning(containerSpace)) {
		//	return false;
		//}
		
		// Get what our subgoal is looking for and make copies
		List<IngredientRecipe> necessaryIngs = new ArrayList<IngredientRecipe>(this.topLevelIngredient.getContents()); 
		necessaryIngs.add(this.topLevelIngredient);
		AbstractMap<String, IngredientRecipe> necessaryTraits = new HashMap<String, IngredientRecipe>(this.topLevelIngredient.getNecessaryTraits());
		
		if (SpaceFactory.isBaking(space)) {
			return this.checkMoveToBaking(state, container, contents);
		} else if (SpaceFactory.isHeating(space)) {
			return this.checkMoveToHeating(state, container, contents);
		//} else if (SpaceFactory.isCleaning(space)) { 
		 //	return this.checkMoveToCleaning(state, container);
		} else if (ContainerFactory.isEmptyContainer(container)){
			return false;
		} else {
			return AllowMoving.allIngredientsNecessary(state, necessaryIngs, container, necessaryTraits);
		}
	}
	
	private static boolean allIngredientsNecessary(State state, List<IngredientRecipe> necessaryIngs, ObjectInstance container, 
			AbstractMap<String, IngredientRecipe> necessaryTraits) {
		
		// Look to see what  ingredients we have already fulfilled in our bowl
		Set<ObjectInstance> receivingContents = new HashSet<ObjectInstance>();
		Set<String> contentNames = ContainerFactory.getConstituentSwappedContentNames(container, state);
		for (String contentName : contentNames) {
			ObjectInstance obj = state.getObject(contentName);
			receivingContents.add(obj);
		}
				
		for (ObjectInstance ingObject : receivingContents) {
			String contentName = ingObject.getName();
			
			IngredientRecipe match = null;
			for (IngredientRecipe ing : necessaryIngs) {
				if (ing.getFullName().equals(contentName) || ing.isMatching(ingObject, state)) {
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
	
	private boolean checkMoveToBaking(State state, ObjectInstance container, Set<String> contents) {
		String ingredientName = topLevelIngredient.getFullName();
		boolean recipeIngBaked = this.topLevelIngredient.getBaked();
		if (!ContainerFactory.isBakingContainer(container)) {
			return false;
		}
		if (recipeIngBaked && contents.contains(ingredientName) ) {
			boolean objIngBaked = IngredientFactory.isBakedIngredient(state.getObject(ingredientName));
			if (!objIngBaked) {
				return true;
			}
		} else {
			List<IngredientRecipe> ingredientContents = this.topLevelIngredient.getContents();
			for (IngredientRecipe ing : ingredientContents) {
				String name = ing.getFullName();
				ObjectInstance obj = state.getObject(name);
				if (obj != null && ing.getBaked() && contents.contains(name)) {
					if (!IngredientFactory.isBakedIngredient(obj)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean checkMoveToHeating(State state, ObjectInstance container, Set<String> contents) {
		String ingredientName = topLevelIngredient.getFullName();
		boolean recipeIngHeated = this.topLevelIngredient.getHeated();
		ObjectInstance topLevelObj = state.getObject(ingredientName);
		if (!ContainerFactory.isHeatingContainer(container)) {
			return false;
		}
		if (recipeIngHeated && contents.contains(ingredientName) ) {
			if (!IngredientFactory.isHeatedIngredient(topLevelObj)) {
				if (!IngredientFactory.isMeltedAtRoomTemperature(topLevelObj)) {
					return true;
				}
			}
		} else {
			List<IngredientRecipe> ingredientContents = this.topLevelIngredient.getContents();
			Map<String, IngredientRecipe> necessaryTraits = this.topLevelIngredient.getNecessaryTraits();
			for (IngredientRecipe ing : ingredientContents) {
				String name = ing.getFullName();
				ObjectInstance obj = state.getObject(name);
				if (ing.getHeated() && contents.contains(name)) {
					if (!IngredientFactory.isHeatedIngredient(obj)) {
						if (!IngredientFactory.isMeltedAtRoomTemperature(obj)) {
							return true;
						}
					}
				}
			}
			for (Map.Entry<String, IngredientRecipe> entry : necessaryTraits.entrySet()) {
				IngredientRecipe ing  = entry.getValue();
				if (ing.getHeated()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean checkMoveToCleaning(State state, ObjectInstance container) {
		return ContainerFactory.getUsed(container) && ContainerFactory.isEmptyContainer(container);
	}

}
