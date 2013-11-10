import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class RecipeTerminalFunction implements TerminalFunction{

	protected Recipe goal;
	PropositionalFunction recipeSuccess;
	PropositionalFunction recipeFailure;
	
	
	public RecipeTerminalFunction(Recipe recipeGoal, PropositionalFunction success, PropositionalFunction failure) {
		this.goal = recipeGoal;
		this.recipeSuccess = success;
		this.recipeFailure = failure;
	}

	@Override
	public boolean isTerminal(State state) {
		return (state.somePFGroundingIsTrue(this.recipeSuccess) ||
				state.somePFGroundingIsTrue(this.recipeFailure));
	}

}
