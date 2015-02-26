package edu.brown.cs.h2r.baking.Agents;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class RandomRecipeAgent extends Human{
	private final Domain domain;
	public RandomRecipeAgent(Domain domain, String name, ActionTimeGenerator timeGenerator) {
		super(domain, name, timeGenerator);
		this.domain = domain;
	}
	
	protected RandomRecipeAgent(Domain domain, Map<String, Object> map, ActionTimeGenerator timeGenerator, State state) {
		super(domain, map, timeGenerator, state);
		this.domain = domain;
	}
	
	@Override
	protected Map<String, Object> toMap() {
		return super.toMap();
	}
	@Override
	public void addObservation(State state) {
	}

	@Override
	public void setInitialState(State state) {
		super.setInitialState(state);
		List<Recipe> recipes = AgentHelper.recipes(this.domain);
		Collections.shuffle(recipes);
		this.setRecipe(recipes.get(0));
	}
	
	@Override
	public AbstractGroundedAction getAction(State state) {
		if (this.currentRecipe == null) {
			List<Recipe> recipes = AgentHelper.recipes(this.domain);
			Collections.shuffle(recipes);
			this.setRecipe(recipes.get(0));
		}
		return super.getAction(state);
	}
}
