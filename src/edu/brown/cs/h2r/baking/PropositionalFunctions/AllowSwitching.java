package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.List;

import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;

public class AllowSwitching extends BakingPropositionalFunction {

	public AllowSwitching(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName}, ingredient);
	}
	
	public boolean isTrue(State state, String[] params) {
		for (BakingSubgoal sg : this.topLevelIngredient.getSubgoals()) {
			// Check if the subgoal is related to the switch action
			if (sg.getGoal().getClassName().equals(AffordanceCreator.SWITCH_PF)) {
				// If the subgoal hasn't been fulfilled by some binding on the state
				if (!sg.goalCompleted(state)) {
					// TODO: Evaluate if this is unnecessary
					if (!sg.getGoal().isTrue(state, params)) {
						return true;
					}
				}
			}
			// Else, check the preconditions for the subgoal
			for (BakingSubgoal precondition : sg.getPreconditions()) {
				// If the preconditions are related to the switching subgoal
				if (precondition.getGoal().getClassName().equals(AffordanceCreator.SWITCH_PF)) {
					// If the precondition hans't been filled up by some biding in the state
					if (!precondition.goalCompleted(state)) {
						// TODO: Evaluate if this check is necessary
						if (!precondition.getGoal().isTrue(state, params)) {
							return true;
						}
					}
				}
			}
		}
		// Since no subgoal for this ingredient is applicable to the switch action, ignore it!
		return false;
	}
}
