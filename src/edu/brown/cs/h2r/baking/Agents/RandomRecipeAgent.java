package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class RandomRecipeAgent extends Human implements Agent {
	private final Domain domain;
	public RandomRecipeAgent(Domain domain, ActionTimeGenerator timeGenerator) {
		super(domain, timeGenerator);
		this.domain = domain;
	}
	@Override
	public void addObservation(State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAgentName() {
		return "partner";
	}

	@Override
	public void setInitialState(State state) {
		super.setInitialState(state);
		super.chooseNewRecipe();
	}

}
