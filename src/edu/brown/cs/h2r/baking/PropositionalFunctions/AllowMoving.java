package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AllowMoving extends BakingPropositionalFunction {

	public AllowMoving(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName}, ingredient) ;
	}
	@Override
	// Default true, since only logic needed is in moveAction's isApplicableInState().
	public boolean isTrue(State s, String[] params) {
		ObjectInstance space = s.getObject(params[2]);
		ObjectInstance container = s.getObject(params[1]);
		
		Set<String> contents = ContainerFactory.getContentNames(container);
		
		/*if (ContainerFactory.isEmptyContainer(container)) {
			return false;
		}
		
		ObjectInstance content = null;
		for (String name : ContainerFactory.getContentNames(container)) {
			content = s.getObject(name);
			break;
		}
		
		if (SpaceFactory.isWorking(space)) {
			ObjectInstance curr_space = s.getObject(ContainerFactory.getSpaceName(container));
			if (SpaceFactory.isBaking(curr_space) && !IngredientFactory.isBakedIngredient(content)) {
				return false;
			}
		}
*/
		// TODO: Consider adding a check to type of container? Also the check for is melted at room temp!
		if (!ContainerFactory.isEmptyContainer(container)) {
			if (SpaceFactory.isBaking(space)) {
				if (this.topLevelIngredient.getBaked() && contents.contains(this.topLevelIngredient.getName()) ) {
					if (!IngredientFactory.isBakedIngredient(s.getObject(topLevelIngredient.getName()))) {
						return true;
					}
				} else {
					for (IngredientRecipe ing : this.topLevelIngredient.getContents()) {
						if (ing.getBaked() && contents.contains(ing.getName())) {
							if (!IngredientFactory.isBakedIngredient(s.getObject(ing.getName()))) {
								return true;
							}
						}
					}
				}
			} else if (SpaceFactory.isHeating(space)) {
				if (this.topLevelIngredient.getMelted() && contents.contains(this.topLevelIngredient.getName()) ) {
					if (!IngredientFactory.isMeltedIngredient(s.getObject(topLevelIngredient.getName()))) {
						if (!IngredientFactory.isMeltedAtRoomTemperature(s.getObject(this.topLevelIngredient.getName()))) {
							return true;
						}
					}
				} else {
					for (IngredientRecipe ing : this.topLevelIngredient.getContents()) {
						if (ing.getMelted() && contents.contains(ing.getName())) {
							if (!IngredientFactory.isMeltedIngredient(s.getObject(ing.getName()))) {
								if (!IngredientFactory.isMeltedAtRoomTemperature(s.getObject(ing.getName()))) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

}
