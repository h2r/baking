package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class RandomRecipeAgent extends Human{
	private final Domain domain;
	public RandomRecipeAgent(Domain domain, String name, ActionTimeGenerator timeGenerator, List<Recipe> recipes) {
		super(domain, name, timeGenerator, recipes);
		this.domain = domain;
	}
	
	protected RandomRecipeAgent(Domain domain, Map<String, Object> map, ActionTimeGenerator timeGenerator, State state, List<Recipe> recipes) {
		super(domain, map, timeGenerator, state, recipes);
		this.domain = domain;
	}
	
	@Override
	protected Map<String, Object> toMap() {
		return super.toMap();
	}
	@Override
	public void addObservation(State state, GroundedAction agentsAction) {
	}

	@Override
	public void setInitialState(State state) {
		super.setInitialState(state);
		List<Recipe> recipes = new ArrayList<Recipe>(this.recipes);
		Collections.shuffle(recipes);
		this.setRecipe(recipes.get(0));
	}
	
	@Override
	public AbstractGroundedAction getActionInState(State state) {
		if (this.currentRecipe == null) {
			List<Recipe> recipes = new ArrayList<Recipe>(this.recipes);
			Collections.shuffle(recipes);
			this.setRecipe(recipes.get(0));
		}
		return super.getActionInState(state);
	}
}
