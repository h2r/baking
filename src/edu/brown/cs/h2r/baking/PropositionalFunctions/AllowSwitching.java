package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.List;

import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AllowSwitching extends BakingPropositionalFunction {
	public AllowSwitching(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName}, ingredient);
		this.subgoal = null;
	}
	
	public boolean isTrue(State state, String[] params) {
		
		ObjectInstance space = state.getObject(params[1]);
		// Since no subgoal for this ingredient is applicable to the switch action, ignore it!
		if (this.subgoal == null) {
			return false;
		}
		if (this.subgoal.getGoal().getClassName().equals(AffordanceCreator.SPACEON_PF)) {
			// If the subgoal hasn't been fulfilled by some binding on the state
			if (!this.subgoal.goalCompleted(state)) {
				return true;
			}
		}
		
		if (!SpaceFactory.getOnOff(space)) {
			if (SpaceFactory.isHeating(space)) {
				if (this.topLevelIngredient.getMelted()) {
					ObjectInstance obj =  state.getObject(this.topLevelIngredient.getName());
					if (obj != null) {
						if (!IngredientFactory.isMeltedIngredient(obj) && !IngredientFactory.isMeltedAtRoomTemperature(obj)) {
							return true;
						}
					}
				}
				
				for (IngredientRecipe ing : this.topLevelIngredient.getContents()) {
					if (ing.getMelted()) {
						ObjectInstance obj = state.getObject(ing.getName());
						if (obj != null) {
							if (!IngredientFactory.isMeltedIngredient(obj) && !IngredientFactory.isMeltedAtRoomTemperature(obj)) {
								return true;
							}
						}
					}
				}
			} else if (SpaceFactory.isBaking(space)) {
				if (this.topLevelIngredient.getBaked()) {
					ObjectInstance obj =  state.getObject(this.topLevelIngredient.getName());
					if (obj != null) {
						if (!IngredientFactory.isBakedIngredient(obj)) {
							return true;
						}
					}
				}
				
				for (IngredientRecipe ing : this.topLevelIngredient.getContents()) {
					if (ing.getBaked()) {
						ObjectInstance obj = state.getObject(ing.getName());
						if (obj != null) {
							if (!IngredientFactory.isBakedIngredient(obj)) {
								return true;
							}
						}
					}
				}
			}
		}
		// Else, check the preconditions for the subgoal
		for (BakingSubgoal precondition : this.subgoal.getPreconditions()) {
			// If the preconditions are related to the grease action
			if (precondition.getGoal().getClassName().equals(AffordanceCreator.SPACEON_PF)) {
				// If the precondition hans't been filled up by some binding in the state
				if (!precondition.goalCompleted(state)) {
					return true;
				}
			}
		}
		return false;
	}
}
