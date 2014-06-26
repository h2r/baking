package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.State;

public class BakingSubgoal {

	private BakingPropositionalFunction goal;
	private List<BakingSubgoal> preconditions;
	private IngredientRecipe ingredient;
	
	public BakingSubgoal(BakingPropositionalFunction pf, IngredientRecipe ingredient) {
		this.goal = pf;
		this.preconditions = new ArrayList<BakingSubgoal>();
		this.ingredient = ingredient;
		//this.ingredient.addSubgoal(this);
	}
	
	public BakingPropositionalFunction getGoal() {
		return this.goal;
	}
	
	public List<BakingSubgoal> getPreconditions() {
		return this.preconditions;
	}
	
	public void addPrecondition(BakingSubgoal sg) {
		this.preconditions.add(sg);
	}
	
	public Boolean goalCompleted(State state) {
		this.goal.changeTopLevelIngredient(this.ingredient);
		Boolean completed = false;
		for (GroundedProp gp : state.getAllGroundedPropsFor(this.goal)) {
			if (gp.isTrue(state)) {
				completed = true;
				break;
			}
		}
		return completed;
	}
	
	public Boolean allPreconditionsCompleted(State state) {
		//IngredientRecipe toDelete = null;
		for (BakingSubgoal sg : this.preconditions) {
			/*if (toDelete != null) {
				this.preconditions.remove(toDelete);
			}*/
			BakingPropositionalFunction pf = sg.getGoal();
			pf.changeTopLevelIngredient(sg.getIngredient());
			Boolean completed = false;
			for (GroundedProp gp : state.getAllGroundedPropsFor(pf)) {
				if (gp.isTrue(state)) {
					completed = true;
					//toDelete = ing;
					break;
				}
			}
			if (!completed) {
				return false;
			}
		}
		return true;
	}
	
	public IngredientRecipe getIngredient() {
		return this.ingredient;
	}
}
