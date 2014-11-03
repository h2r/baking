package edu.brown.cs.h2r.baking.Experiments;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class KitchenSubdomain {

	Recipe recipe;
	BakingSubgoal subgoal;
	State startState;
	Policy policy;
	Domain domain;
	
	private KitchenSubdomain(Domain domain, Recipe recipe, BakingSubgoal subgoal, State startState, Policy policy) {
		this.domain = domain;
		this.recipe = recipe;
		this.subgoal = subgoal;
		this.startState = startState.copy();
		this.policy = policy;
	}
	
	public static KitchenSubdomain makeSubdomain(Domain domain, Recipe recipe, BakingSubgoal subgoal, State startState, Policy policy) {
		if (subgoal == null || startState == null || policy == null )
		{
			return null;
		}
		return new KitchenSubdomain(domain, recipe, subgoal, startState, policy);
	}

	public Domain getDomain() {
		return this.domain;
	}
	public Recipe getRecipe() {
		return this.recipe;
	}
	
	public BakingSubgoal getSubgoal() {
		return this.subgoal;
	}
	
	public TerminalFunction getTerminalFunction() {
		return this.subgoal.getTerminalFunction(this.domain);
	}
	
	public State getStartState() {
		return this.startState.copy();
	}
	
	public Policy getPolicy() {
		return this.policy;
	}
	
	@Override
	public String toString() {
		return this.recipe.toString() + " - " + this.subgoal.toString();
	}
	
}
