package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class RandomActionCorrectSubgoal extends Human {
	private final Domain domain;
	Random random;
	public RandomActionCorrectSubgoal(Domain domain, String name, ActionTimeGenerator timeGenerator, List<Recipe> recipes) {
		super(domain, name, timeGenerator, recipes);
		this.domain = domain;
		this.random = new Random();
	}
	@Override
	public void addObservation(State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInitialState(State state) {
		super.setInitialState(state);
		super.chooseNewRecipe();
	}
	
	@Override
	public AbstractGroundedAction getActionInState(State state) {
		if (this.isSuccess(state)) {
			return null;
		}
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(state);
		} else if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
			this.getKitchenSubdomains().remove(this.currentSubgoal);
			this.chooseNewSubgoal(state);
		}
		if (this.currentSubgoal == null) {
			this.setRecipe(this.currentRecipe);
			return new GroundedAction(this.generalDomain.getAction("reset"), new String[] {"human"});
		}
		
		List<ActionProb> allowableActions = this.getAllowableActions(state);
		
		if (allowableActions.size() == 0) {
			this.chooseNewSubgoal(state);
			if (this.currentSubgoal == null) {
				return null;
			}
			allowableActions = this.getAllowableActions(state);
		}
		List<ActionProb> nonZero = new ArrayList<ActionProb>(allowableActions.size());
		for (ActionProb actionProb : allowableActions) {
			if (actionProb.pSelection > 0.0) {
				nonZero.add(actionProb);
			}
		}
		if (nonZero.isEmpty()) {
			return null;
		}
		int choice = this.random.nextInt(nonZero.size());
		
		AbstractGroundedAction action = nonZero.get(choice).ga;
		if (action != null) {
			GroundedAction groundedAction = (GroundedAction)action;
			groundedAction.params[0] = this.getAgentName();
		}
		
		return action;
	}
	

}
