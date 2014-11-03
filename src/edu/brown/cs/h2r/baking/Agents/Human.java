package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import Prediction.PolicyPrediction;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.PeanutButterCookies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class Human implements Agent {
	private final static RewardFunction rewardFunction = new RecipeAgentSpecificMakeSpanRewardFunction(Human.HUMAN_NAME);
	
	public final static String HUMAN_NAME = "human";
	private final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	private State startingState;
	
	
	private Recipe currentRecipe;
	private KitchenSubdomain currentSubgoal;
	private List<KitchenSubdomain> kitchenSubdomains;
	private TerminalFunction isFailure;
	private final Domain generalDomain;
	
	public Human(Domain generalDomain) {
		this.generalDomain = generalDomain;
	}
	
	@Override
	public ObjectInstance getAgentObject() {
		return AgentFactory.getNewHumanAgentObjectInstance(this.generalDomain, this.getAgentName());
	}
	
	@Override
	public void setInitialState(State state) {
		this.startingState = state;
	}
	
	public void chooseNewRecipe() {
		List<Recipe> recipes = new ArrayList<Recipe>(AgentHelper.recipes());
		Collections.shuffle(recipes);
		this.currentRecipe = recipes.get(0);
		
		Domain specificDomain = AgentHelper.generateSpecificDomain(generalDomain, this.currentRecipe);
		this.kitchenSubdomains = AgentHelper.generateRTDPPolicies(this.currentRecipe, specificDomain, this.startingState, Human.rewardFunction, Human.hashingFactory);
	}
	
	private void chooseNewSubgoal(State state) {
		List<KitchenSubdomain> activeSubgoals = new ArrayList<KitchenSubdomain>();
		
		for (KitchenSubdomain subdomain : this.kitchenSubdomains) {
			if (subdomain.getSubgoal().allPreconditionsCompleted(state)) {
				activeSubgoals.add(subdomain);
				
			}
		}
		
		Collections.shuffle(activeSubgoals);
		this.currentSubgoal = (activeSubgoals.size() == 0) ? null : activeSubgoals.get(0);
		if (this.currentSubgoal == null) {
			return;
		}
		
		final PropositionalFunction isSuccess = this.currentSubgoal.getSubgoal().getGoal();
		final PropositionalFunction isFailure = this.currentSubgoal.getDomain().getPropFunction(AffordanceCreator.BOTCHED_PF);
		this.isFailure = new RecipeTerminalFunction(isFailure);
		
		AgentHelper.setSubgoal(this.currentSubgoal);
		
		
	}
	
	
	
	@Override
	public void addObservation(State state, GroundedAction action) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAgentName() {
		return Human.HUMAN_NAME;
	}

	@Override
	public AbstractGroundedAction getAction(State state) {
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(state);
		}
		if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
			this.kitchenSubdomains.remove(this.currentSubgoal);
			this.chooseNewSubgoal(state);
			System.out.println("Human switches to task: " + this.currentSubgoal.toString());
		}
		if (this.currentSubgoal == null) {
			return null;
		}
		if (this.isFinished(state)) {
			return null;
		}
		PolicyPrediction.setSubgoal(this.currentSubgoal);
		Policy policy = this.currentSubgoal.getPolicy();
		AbstractGroundedAction action = policy.getAction(state);
		return action;
	}

	public boolean isFinished(State state) {
		boolean isFailure = this.isFailure.isTerminal(state);
		boolean isGoalComplete = this.currentSubgoal.getSubgoal().goalCompleted(state);
		boolean areGoalsCompleted = this.kitchenSubdomains.size() == 1;
		return (isFailure || (isGoalComplete && areGoalsCompleted)); 
	}
	
	public double getCostActions(List<AbstractGroundedAction> actionSequence, List<State> stateSequence) {
		double cost = 0.0;
		State previousState = this.startingState;
		for (int i = 0; i < actionSequence.size(); i++) {
			GroundedAction groundedAction = (GroundedAction)actionSequence.get(i);
			State nextState = stateSequence.get(i+1);
			cost += this.rewardFunction.reward(previousState, groundedAction, nextState);
		
		}
		return cost;
	}
}
