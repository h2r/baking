package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.LogicalExpression;

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
	
	public LogicalExpression getLogicalPrecondition() {
		LogicalExpression expression = new LogicalExpression() {
			
			@Override
			protected void remapVariablesInThisExpression(
					Map<String, String> fromToVariableMap) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean evaluateIn(State s) {
				return BakingSubgoal.this.goal.isTrue(s, "");
			}
			
			@Override
			public LogicalExpression duplicate() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		return expression;
	}
}
