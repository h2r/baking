package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class BakingSubgoal {

	private final BakingPropositionalFunction goal;
	private final List<BakingSubgoal> preconditions;
	private final IngredientRecipe ingredient;
	
	public BakingSubgoal(BakingPropositionalFunction pf, IngredientRecipe ingredient) {
		this.goal = pf;
		this.preconditions = Collections.unmodifiableList(new ArrayList<BakingSubgoal>());
		this.ingredient = ingredient;
	}
	
	public BakingSubgoal(BakingPropositionalFunction pf, IngredientRecipe ingredient, List<BakingSubgoal> preconditions) {
		this.goal = pf;
		this.preconditions = Collections.unmodifiableList(preconditions);
		this.ingredient = ingredient;
	}
	
	public BakingSubgoal(BakingSubgoal subgoal) {
		this.goal = subgoal.goal;
		this.preconditions = Collections.unmodifiableList(subgoal.preconditions);
		this.ingredient = subgoal.ingredient;
	}
	
	public BakingSubgoal(BakingSubgoal subgoal, List<BakingSubgoal> preconditions) {
		this.goal = subgoal.goal;
		this.preconditions = Collections.unmodifiableList(preconditions);
		this.ingredient = subgoal.ingredient;
	}
	
	public static BakingSubgoal fromMap(Map<String, Object> map, Domain domain, Recipe recipe) {
		String className = (String)map.get("pf_class");
		BakingPropositionalFunction pf = null;
		Map<String, Object> ingredientMap = (Map<String, Object>)map.get("ingredient");
		IngredientRecipe ingredient = IngredientRecipe.fromMap(ingredientMap, recipe);
		if (className.equals("RecipeFinished")) {
			String pfName = (String)map.get("pf_name");
			pf = new RecipeFinished(pfName, domain, ingredient);
		}
		List<BakingSubgoal> preconditions = new ArrayList<BakingSubgoal>();
		List<Map<String, Object>> preconditionMaps = (List<Map<String, Object>>)map.get("preconditions");
		for (Map<String, Object> preconditionMap : preconditionMaps) {
			preconditions.add(BakingSubgoal.fromMap(preconditionMap, domain, recipe));
		}
		
		return new BakingSubgoal(pf, ingredient, preconditions);
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pf_class", this.goal.getClass().getSimpleName());
		map.put("pf_name", this.goal.getName());
		List<String> preconditions = new ArrayList<String>();
		for (BakingSubgoal subgoal : this.preconditions) {
			preconditions.add(subgoal.toString());
		}
		map.put("preconditions", preconditions);
		map.put("ingredient", this.ingredient.toMap());
		
		return map;
	}
	
	public BakingPropositionalFunction getGoal() {
		return this.goal;
	}
	
	public List<BakingSubgoal> getPreconditions() {
		return Collections.unmodifiableList(this.preconditions);
	}
	
	public BakingSubgoal addPrecondition(BakingSubgoal sg) {
		List<BakingSubgoal> preconditions = new ArrayList<BakingSubgoal>(this.preconditions);
		preconditions.add(sg);
		return new BakingSubgoal(this, preconditions);
	}
	
	
	@Override
	public String toString() {
		return this.getIngredient().getFullName();
	}
	public Boolean goalCompleted(State state) {
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
