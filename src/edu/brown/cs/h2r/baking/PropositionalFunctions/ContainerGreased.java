package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;

public class ContainerGreased extends BakingPropositionalFunction {

	public ContainerGreased(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName }, ingredient);
	}
	
	public boolean isTrue(State state, String[] params) {
		ObjectInstance container = state.getObject(params[1]);
		return ContainerFactory.isGreasedContainer(container);
	}
}
