package edu.brown.cs.h2r.baking;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class RecipeTerminalFunction implements TerminalFunction{

	PropositionalFunction recipeSuccess;
	PropositionalFunction recipeFailure;
	PropositionalFunction bowlsClean;
	
	public RecipeTerminalFunction(PropositionalFunction success, PropositionalFunction failure) {
		this.recipeSuccess = success;
		this.recipeFailure = failure;
	}
	
	
	public RecipeTerminalFunction(PropositionalFunction bowlsClean,
			PropositionalFunction success, PropositionalFunction failure) {
		this.bowlsClean = bowlsClean;
		this.recipeSuccess = success;
		this.recipeFailure = failure;
	}

	@Override
	public boolean isTerminal(State state) {
		for (GroundedProp cleangp : this.bowlsClean.getAllGroundedPropsForState(state)) {
			if (!cleangp.isTrue(state)) {
				return false;
			}
		}
		return (this.recipeSuccess.somePFGroundingIsTrue(state) ||
				this.recipeFailure.somePFGroundingIsTrue(state));
	}

}
