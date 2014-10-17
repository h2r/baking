package edu.brown.cs.h2r.baking.PropositionalFunctions;

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

public class AllowMoving extends BakingPropositionalFunction {

	public AllowMoving(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName}, ingredient) ;
	}
	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance space = state.getObject(params[2]);
		ObjectInstance container = state.getObject(params[1]);		
		Set<String> contents = ContainerFactory.getContentNames(container);
		
		
		if (!ContainerFactory.isEmptyContainer(container)) {
			if (SpaceFactory.isBaking(space)) {
				return this.checkMoveToBaking(state, contents);
			} else if (SpaceFactory.isHeating(space)) {
				return this.checkMoveToHeating(state, contents);
			} else {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkMoveToBaking(State state, Set<String> contents) {
		String ingredientName = topLevelIngredient.getName();
		boolean recipeIngBaked = this.topLevelIngredient.getBaked();
		if (recipeIngBaked && contents.contains(ingredientName) ) {
			boolean objIngBaked = IngredientFactory.isBakedIngredient(state.getObject(ingredientName));
			if (!objIngBaked) {
				return true;
			}
		} else {
			List<IngredientRecipe> ingredientContents = this.topLevelIngredient.getContents();
			for (IngredientRecipe ing : ingredientContents) {
				String name = ing.getName();
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
	
	private boolean checkMoveToHeating(State state, Set<String> contents) {
		String ingredientName = topLevelIngredient.getName();
		boolean recipeIngHeated = this.topLevelIngredient.getHeated();
		ObjectInstance topLevelObj = state.getObject(ingredientName);
		if (recipeIngHeated && contents.contains(ingredientName) ) {
			if (!IngredientFactory.isHeatedIngredient(topLevelObj)) {
				if (!IngredientFactory.isMeltedAtRoomTemperature(topLevelObj)) {
					return true;
				}
			}
		} else {
			List<IngredientRecipe> ingredientContents = this.topLevelIngredient.getContents();
			for (IngredientRecipe ing : ingredientContents) {
				String name = ing.getName();
				ObjectInstance obj = state.getObject(name);
				if (ing.getHeated() && contents.contains(name)) {
					if (!IngredientFactory.isHeatedIngredient(obj)) {
						if (!IngredientFactory.isMeltedAtRoomTemperature(obj)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
