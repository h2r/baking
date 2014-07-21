package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class AllowGreasing extends BakingPropositionalFunction {
	public AllowGreasing(String name, Domain domain,  IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, IngredientFactory.ClassNameSimple},ingredient);
		this.subgoal = null;
	}

	public boolean isTrue(State state, String[] params) {
		// Since no subgoal for this ingredient is applicable to the switch action, ignore it!
		if (this.subgoal == null) {
			return false;
		}
		if (this.topLevelIngredient == null) {
			return true;
		}
		String subgoalClassName = this.subgoal.getGoal().getClassName();
		if (subgoalClassName.equals(AffordanceCreator.CONTAINERGREASED_PF)) {
			// If the subgoal hasn't been fulfilled by some binding on the state
			if (!this.subgoal.goalCompleted(state)) {
				return true;
			}
		}
		// Else, check the preconditions for the subgoal
		List<BakingSubgoal> preconditions = this.subgoal.getPreconditions();
		for (BakingSubgoal precondition : preconditions) {
			// If the preconditions are related to the grease action
			String preconditionClassName = precondition.getGoal().getClassName();
			if (preconditionClassName.equals(AffordanceCreator.CONTAINERGREASED_PF)) {
				// If the precondition hans't been filled up by some binding in the state
				if (!precondition.goalCompleted(state)) {
					return true;
				}
			}
		}
		return false;
	}
}
