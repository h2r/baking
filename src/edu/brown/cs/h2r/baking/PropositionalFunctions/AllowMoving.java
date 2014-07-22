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
			return true;
		}
		return false;
	}
}
