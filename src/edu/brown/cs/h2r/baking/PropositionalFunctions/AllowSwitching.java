package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.parallel.Parallel.V;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class AllowSwitching extends BakingPropositionalFunction {
	public AllowSwitching(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, SpaceFactory.ClassName}, ingredient);
	}
	
	public BakingPropositionalFunction updatePF(Domain newDomain, IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return new AllowSwitching(this.name, newDomain, ingredient);
	}
	
	
	public boolean isTrue(State state, String[] params) {
		
		ObjectInstance space = state.getObject(params[1]);
		// Since no subgoal for this ingredient is applicable to the switch action, ignore it!
		//if (this.subgoal == null) {
		//	return false;
		//}
		/*String subgoalClassName = this.subgoal.getGoal().getClassName();
		if (subgoalClassName.equals(AffordanceCreator.SPACEON_PF)) {
			// If the subgoal hasn't been fulfilled by some binding on the state
			if (!this.subgoal.goalCompleted(state)) {
				return true;
			}
		}*/
		
		if (!SpaceFactory.getOnOff(space)) {
			// topLevelIngredient's .getRecipeHeated return whether, at some point in the recipe, an
			// ingredient has to be heated. If so, then the next checkSwitchHeating determines if the
			// ingredient(s) that had to be heated has already been heated. The same applies for baking.
			if (SpaceFactory.isHeating(space) && this.topLevelIngredient.getRecipeHeated()) {
				return this.checkSwitchHeating(state, space);
			} else if (SpaceFactory.isBaking(space) && this.topLevelIngredient.getRecipeBaked()) {
				return this.checkSwitchBaking(state, space);
			}
		}
		return false;
	}
	
	private boolean checkSwitchHeating(State state, ObjectInstance space) {
		if (this.topLevelIngredient.getHeated()) {
			ObjectInstance obj =  state.getObject(this.topLevelIngredient.getFullName());
			if (obj != null) {
				if (!IngredientFactory.isHeatedIngredient(obj) && !IngredientFactory.isMeltedAtRoomTemperature(obj)) {
					return true;
				}
			}
		}
		List<IngredientRecipe> contents = this.topLevelIngredient.getContents();
		Map<String, IngredientRecipe> traits = this.topLevelIngredient.getNecessaryTraits();
		for (IngredientRecipe ing : contents) {
			if (ing.getHeated()) {
				ObjectInstance obj = state.getObject(ing.getFullName());
				if (obj != null) {
					if (!IngredientFactory.isHeatedIngredient(obj) && !IngredientFactory.isMeltedAtRoomTemperature(obj)) {
						return true;
					}
				}
			}
		}
		for (Map.Entry<String, IngredientRecipe> entry : traits.entrySet()) {
			IngredientRecipe trait = entry.getValue();
			if (trait.getHeated()) {
				return true;
			}
		}
		return false;
	}
	
	// Why doesn't it switch on if no complex ingredients exist?
	private boolean checkSwitchBaking(State state, ObjectInstance space) {
		if (this.topLevelIngredient.getBaked()) {
			return !SpaceFactory.getOnOff(space);
			//ObjectInstance obj =  state.getObject(this.topLevelIngredient.getName());
			//if (obj != null) {
			//	if (!IngredientFactory.isBakedIngredient(obj)) {
			//		return true;
			//	}
			//}
		}
		
		List<IngredientRecipe> contents = this.topLevelIngredient.getContents();
		for (IngredientRecipe ing : contents) {
			if (ing.getBaked()) {
				return !SpaceFactory.getOnOff(space);
				//ObjectInstance obj = state.getObject(ing.getName());
				//if (obj != null) {
				//	if (!IngredientFactory.isBakedIngredient(obj)) {
				//		return true;
				//	}
				//}
			}
		}
		return false;
	}
	
	
}
