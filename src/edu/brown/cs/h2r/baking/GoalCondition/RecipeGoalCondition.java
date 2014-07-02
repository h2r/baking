package edu.brown.cs.h2r.baking.GoalCondition;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class RecipeGoalCondition implements StateConditionTest {
	PropositionalFunction propositionalFunction;
	public RecipeGoalCondition(PropositionalFunction pf) {
		this.propositionalFunction = pf;
	}
	@Override
	public boolean satisfies(State s) {
		return this.propositionalFunction.somePFGroundingIsTrue(s);
	}

}
