package edu.brown.cs.h2r.baking.PropositionalFunctions;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AllowMoving extends BakingPropositionalFunction {

	public AllowMoving(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName}, ingredient) ;
	}
	@Override
	// Default true for now until I find better logic.
	public boolean isTrue(State s, String[] params) {
		// TODO Auto-generated method stub
		String spaceName = params[2];
		ObjectInstance space = s.getObject(spaceName);
		String agentName = SpaceFactory.getAgent(space).iterator().next();
		if (agentName != params[0]) {
			//return false;
		}
		ObjectInstance container = s.getObject(params[1]);
		if (ContainerFactory.getSpaceName(container).equals(spaceName)) {
			return false;
		}
		return true;
	}

}
