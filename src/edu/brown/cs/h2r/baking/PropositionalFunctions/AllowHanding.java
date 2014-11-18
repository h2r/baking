package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;

public class AllowHanding extends BakingPropositionalFunction {

	public AllowHanding(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ToolFactory.ClassName, SpaceFactory.ClassName}, ingredient) ;
	}
	
	public BakingPropositionalFunction updatePF(Domain newDomain, IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return new AllowHanding(this.name, newDomain, ingredient);
	}
	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance space = state.getObject(params[2]);
		ObjectInstance tool = state.getObject(params[1]);		
		return params[2].equals(SpaceFactory.SPACE_COUNTER) && params[0].equals("baxter");
	}
	
		
	private boolean checkMoveToCleaning(State state, ObjectInstance container) {
		return ContainerFactory.getUsed(container) && ContainerFactory.isEmptyContainer(container);
	}

}
