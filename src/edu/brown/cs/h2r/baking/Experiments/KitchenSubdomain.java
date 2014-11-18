package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class KitchenSubdomain {

	private final Recipe recipe;
	private final BakingSubgoal subgoal;
	private final State startState;
	private final Policy policy;
	private final Domain domain;
	
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
		
		IngredientRecipe ingredient = subgoal.getIngredient();
		
		SADomain newDomain = new SADomain((SADomain)domain);
		
		List<PropositionalFunction> propFunctions = domain.getPropFunctions();
		List<PropositionalFunction> newPropFunctions = new ArrayList<PropositionalFunction>();
		for (PropositionalFunction pf : propFunctions) {
			BakingPropositionalFunction oldPf = (BakingPropositionalFunction)pf;
			newPropFunctions.add(oldPf.updatePF(newDomain, ingredient, subgoal));
		}
		
		return new KitchenSubdomain(new SADomain(newDomain, propFunctions), recipe, subgoal, startState, policy);
	}
	
	public static KitchenSubdomain makeSubdomain(KitchenSubdomain other) {
		return new KitchenSubdomain(other.domain, other.recipe, other.subgoal, other.startState, other.policy);
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

	public boolean isValidInState(State state) {
		return this.subgoal.allPreconditionsCompleted(state) && !this.subgoal.goalCompleted(state);
	}
	
	@Override
	public String toString() {
		return this.recipe.toString() + " - " + this.subgoal.toString();
	}
	
}
