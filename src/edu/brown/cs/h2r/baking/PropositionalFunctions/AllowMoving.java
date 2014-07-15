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
	public boolean isTrue(State s, String[] params) {
		ObjectInstance space = s.getObject(params[2]);
		ObjectInstance container = s.getObject(params[1]);
		ObjectInstance currentSpace = s.getObject(ContainerFactory.getSpaceName(container));
		
		Set<String> contents = ContainerFactory.getContentNames(container);
		
		
		if (!ContainerFactory.isEmptyContainer(container)) {
			if (SpaceFactory.isBaking(space)) {
				return this.checkMoveToBaking(s, contents);
			} else if (SpaceFactory.isHeating(space)) {
				return this.checkMoveToHeating(s, contents);
			} else {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkMoveToBaking(State s, Set<String> contents) {
		if (this.topLevelIngredient.getBaked() && contents.contains(this.topLevelIngredient.getName()) ) {
			if (!IngredientFactory.isBakedIngredient(s.getObject(topLevelIngredient.getName()))) {
				return true;
			}
		} else {
			List<IngredientRecipe> ingredientContents = this.topLevelIngredient.getContents();
			for (IngredientRecipe ing : ingredientContents) {
				if (ing.getBaked() && contents.contains(ing.getName())) {
					if (!IngredientFactory.isBakedIngredient(s.getObject(ing.getName()))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean checkMoveToHeating(State s, Set<String> contents) {
		if (this.topLevelIngredient.getHeated() && contents.contains(this.topLevelIngredient.getName()) ) {
			if (!IngredientFactory.isHeatedIngredient(s.getObject(topLevelIngredient.getName()))) {
				if (!IngredientFactory.isMeltedAtRoomTemperature(s.getObject(this.topLevelIngredient.getName()))) {
					return true;
				}
			}
		} else {
			List<IngredientRecipe> ingredientContents = this.topLevelIngredient.getContents();
			for (IngredientRecipe ing : ingredientContents) {						if (ing.getHeated() && contents.contains(ing.getName())) {
					if (!IngredientFactory.isHeatedIngredient(s.getObject(ing.getName()))) {
						if (!IngredientFactory.isMeltedAtRoomTemperature(s.getObject(ing.getName()))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
