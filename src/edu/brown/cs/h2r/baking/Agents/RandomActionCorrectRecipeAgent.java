package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class RandomActionCorrectRecipeAgent extends Human{
	private final Domain domain;
	private Random random;
	public RandomActionCorrectRecipeAgent(Domain domain, String name, ActionTimeGenerator timeGenerator) {
		super(domain, name, timeGenerator);
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
	}
	
	@Override
	public AbstractGroundedAction getAction(State state) {
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
		
		List<ActionProb> allowableActions = new ArrayList<ActionProb>();
		for (KitchenSubdomain subdomain : this.kitchenSubdomains) {
			AffordanceRTDP planner = subdomain.getPlanner();
			planner.planFromState(state);
			Policy policy = subdomain.getPolicy();
			allowableActions.addAll(policy.getActionDistributionForState(state));
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
