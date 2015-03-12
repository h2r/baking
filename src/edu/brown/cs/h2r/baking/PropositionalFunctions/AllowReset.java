package edu.brown.cs.h2r.baking.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;

public class AllowReset extends BakingPropositionalFunction {
	private State startState;
	public AllowReset(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName}, ingredient);
	}
	@Override
	public BakingPropositionalFunction updatePF(Domain newDomain,
			IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return this;
	}

	public void setStartState(State startingState) {
		this.startState = startingState;
	}
	
	@Override
	public boolean isTrue(State s, String[] params) {
		if (this.startState != null && this.startState.equals(s)) {
			return false;
		}
		if (!params[0].equals("human")) {
			return false;
		}
		return true;
	}

}
