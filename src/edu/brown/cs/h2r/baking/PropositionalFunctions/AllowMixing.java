package edu.brown.cs.h2r.baking.PropositionalFunctions;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AllowMixing extends BakingPropositionalFunction {

	public AllowMixing(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName}, ingredient) ;
	}
	@Override
	// Default true for now until I find better logic.
	public boolean isTrue(State state, String[] params) {
		ObjectInstance containerInstance = state.getObject(params[1]);
		int contentAmount = ContainerFactory.getContentNames(containerInstance).size();
		int neededAmount =  this.topLevelIngredient.getContents().size() 
				+ this.topLevelIngredient.getNecessaryTraits().size();
		
		if (contentAmount != neededAmount) {
			return false;
		}
		
		return true;
	}

}
