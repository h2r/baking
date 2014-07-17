package edu.brown.cs.h2r.baking.PropositionalFunctions;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class RecipeBotched extends BakingPropositionalFunction {

	List<BakingSubgoal> ingSubgoals;
	public RecipeBotched(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName}, ingredient);
		this.ingSubgoals = new ArrayList<BakingSubgoal>();
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		List<ObjectInstance> ingredients = state.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex);
		boolean failed; // has it failed with at least one subgoal?
		boolean notFail; // has it not failed with a least one subgoal?
		for (ObjectInstance ingredient : ingredients) {
			failed = false;
			notFail = false;
			for (BakingSubgoal sg : this.ingSubgoals) {
				IngredientRecipe goalIng = sg.getIngredient();
				// Fake copy so a final ingredient that has yet to be baked would not return failure.
				// Conversely, if a non-top level ingredient "gets by" without being melted or baked,
				// then the failure will be detected once that ingredient is added to some other ingredient,
				// and this large ingredient is checked for failure.
				if (Recipe.isFailure(state, goalIng.makeFakeFailureCopy(ingredient), ingredient)) {
					failed = true;
				} else {
					notFail = true;
				}
			}
			// If it has failed, and hasn't not_failed, then the recipe is most definitely a failure.
			if (failed && !notFail) {
				return true;
			}
		}
		return false;
	}
	
	public void addSubgoal(BakingSubgoal sg) {
		this.ingSubgoals.add(sg);
	}
	
	public boolean hasNoSubgoals() {
		return this.ingSubgoals.isEmpty();
	}
	
	public void clearSubgoals() {
		this.ingSubgoals.clear();
	}

}
