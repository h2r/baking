package edu.brown.cs.h2r.baking;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class RecipeTerminalFunction implements TerminalFunction{

	PropositionalFunction recipeSuccess;
	PropositionalFunction recipeFailure;
	
	
	public RecipeTerminalFunction(PropositionalFunction success, PropositionalFunction failure) {
		this.recipeSuccess = success;
		this.recipeFailure = failure;
	}

	@Override
	public boolean isTerminal(State state) {
		return (this.recipeSuccess.somePFGroundingIsTrue(state) ||
				this.recipeFailure.somePFGroundingIsTrue(state));
	}

}
