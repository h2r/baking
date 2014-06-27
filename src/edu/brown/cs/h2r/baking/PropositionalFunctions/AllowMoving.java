package edu.brown.cs.h2r.baking.PropositionalFunctions;

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
		/*String spaceName = params[2];
		ObjectInstance space = s.getObject(spaceName);
		ObjectInstance container = s.getObject(params[1]);
		
		if (ContainerFactory.isEmptyContainer(container)) {
			return false;
		}
		
		ObjectInstance content = null;
		for (String name : ContainerFactory.getContentNames(container)) {
			content = s.getObject(name);
			break;
		}
		
		if (SpaceFactory.isBaking(space)) {
			if (!ContainerFactory.isBakingContainer(container)) {
				return false;
			}
			if (IngredientFactory.isSimple(content)) {
				return false;
			}
			if (IngredientFactory.isBakedIngredient(content)) {
				return false;
			}
		}
		
		if (SpaceFactory.isWorking(space)) {
			ObjectInstance curr_space = s.getObject(ContainerFactory.getSpaceName(container));
			if (SpaceFactory.isBaking(curr_space) && !IngredientFactory.isBakedIngredient(content)) {
				return false;
			}
		}
		
		if (SpaceFactory.isHeating(space) && !ContainerFactory.isHeatingContainer(container)) {
			return false;
		}
		
		return true;*/
		return false;
	}

}
