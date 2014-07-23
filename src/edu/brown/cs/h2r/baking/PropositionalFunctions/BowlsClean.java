package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class BowlsClean extends BakingPropositionalFunction {

	public BowlsClean(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName}, ingredient) ;
	}
	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance container = state.getObject(params[1]);		
		if (ContainerFactory.getUsed(container) && 
				ContainerFactory.isEmptyContainer(container)) {
			if (!ContainerFactory.getSpaceName(container).equals(SpaceFactory.SPACE_DIRTY)) {
				return false;
			}
		}
		return true;
	}
}
