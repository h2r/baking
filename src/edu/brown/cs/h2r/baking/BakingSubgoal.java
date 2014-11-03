package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;

public class BakingSubgoal {

	private BakingPropositionalFunction goal;
	private List<BakingSubgoal> preconditions;
	private IngredientRecipe ingredient;
	
	public BakingSubgoal(BakingPropositionalFunction pf, IngredientRecipe ingredient) {
		this.goal = pf;
		this.preconditions = new ArrayList<BakingSubgoal>();
		this.ingredient = ingredient;
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
	
	
	@Override
	public String toString() {
		return this.getIngredient().getName();
	}
	public Boolean goalCompleted(State state) {
		this.goal.changeTopLevelIngredient(this.ingredient);
		Boolean completed = false;
		for (GroundedProp gp : this.goal.getAllGroundedPropsForState(state)) {
			if (gp.isTrue(state)) {
				completed = true;
				break;
			}
		}
		return completed;
	}
	
	public Boolean allPreconditionsCompleted(State state) {
		for (BakingSubgoal sg : this.preconditions) {
			BakingPropositionalFunction pf = sg.getGoal();
			pf.changeTopLevelIngredient(sg.getIngredient());
			Boolean completed = false;
			for (GroundedProp gp : pf.getAllGroundedPropsForState(state)) {
				if (gp.isTrue(state)) {
					completed = true;
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
	
	public TerminalFunction getTerminalFunction(Domain domain) {
		final PropositionalFunction isSuccess = this.getGoal();
		final PropositionalFunction isFailure = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		return new RecipeTerminalFunction(isSuccess, isFailure);
	}
}
