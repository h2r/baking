package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.ObjectHashFactory;
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
import edu.brown.cs.h2r.baking.Prediction.PolicyPrediction;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.PeanutButterCookies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class Human implements Agent {
	private final static RewardFunction rewardFunction = new RecipeAgentSpecificMakeSpanRewardFunction(Human.HUMAN_NAME);
	
	public final static String HUMAN_NAME = "human";
	private String name = Human.HUMAN_NAME;
	private final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	private State startingState;
	
	
	private Recipe currentRecipe;
	private KitchenSubdomain currentSubgoal;
	private List<KitchenSubdomain> kitchenSubdomains;
	private TerminalFunction isFailure;
	private Domain generalDomain;
	
	public Human(Domain generalDomain) {
		this.generalDomain = generalDomain;
	}
	
	public Human(Domain generalDomain, String name) {
		this.generalDomain = generalDomain;
		this.name = name;
	}
	
	@Override
	public ObjectInstance getAgentObject() {
		return AgentFactory.getNewHumanAgentObjectInstance(this.generalDomain, this.getAgentName(), hashingFactory.getObjectHashFactory());
	}
	
	@Override
	public void setInitialState(State state) {
		this.startingState = state;
	}
	
	public void chooseNewRecipe() {
		List<Recipe> recipes = new ArrayList<Recipe>(AgentHelper.recipes(generalDomain));
		//Collections.shuffle(recipes);
		
		this.setRecipe(recipes.get(0));
	}
	
	public void setRecipe(Recipe recipe) {
		this.currentRecipe = recipe;
		this.currentSubgoal = null;
		Domain specificDomain = AgentHelper.generateSpecificDomain(generalDomain, this.currentRecipe);
		this.kitchenSubdomains = AgentHelper.generateRTDPPolicies(this.currentRecipe, specificDomain, this.startingState, Human.rewardFunction, Human.hashingFactory);
	}
	
	
	
	public Recipe getCurrentRecipe() {
		return this.currentRecipe;
	}
	
	private void chooseNewSubgoal(State state) {
		List<KitchenSubdomain> activeSubgoals = new ArrayList<KitchenSubdomain>();
		
		for (KitchenSubdomain subdomain : this.kitchenSubdomains) {
			if (subdomain.getSubgoal().allPreconditionsCompleted(state) && 
					!subdomain.getSubgoal().goalCompleted(state)) {
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
		
		this.generalDomain = AgentHelper.setSubgoal(this.generalDomain, this.currentSubgoal.getSubgoal());
		//System.out.println(this.getAgentName() + " switches to task: " + this.currentSubgoal.toString());
		
	}
	
	
	
	@Override
	public void addObservation(State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAgentName() {
		return this.name;
	}

	@Override
	public AbstractGroundedAction getAction(State state) {
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(state);
		}
		if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
			this.kitchenSubdomains.remove(this.currentSubgoal);
			this.chooseNewSubgoal(state);
		}
		if (this.currentSubgoal == null) {
			return null;
		}
		if (this.isFinished(state)) {
			return null;
		}
		
		Policy policy = this.currentSubgoal.getPolicy();
		List<ActionProb> actionDistribution = policy.getActionDistributionForState(state);
		List<ActionProb> allowableActions = new ArrayList<ActionProb>();
		for (ActionProb actionProb : actionDistribution) {
			GroundedAction groundedAction = (GroundedAction)actionProb.ga;
			if (groundedAction.params[0].equals(this.getAgentName())) {
				allowableActions.add(actionProb);
			}
		}
		this.normalizeActionDistribution(allowableActions);
		AbstractGroundedAction action = this.getActionFromPolicyDistribution(allowableActions);
		if (action != null) {
			GroundedAction groundedAction = (GroundedAction)action;
			groundedAction.params[0] = this.name;
		}
		return action;
	}
	
	private void normalizeActionDistribution(List<ActionProb> actionDistribution) {
		double sumProbability = 0.0;
		for (ActionProb actionProb : actionDistribution) {
			sumProbability += actionProb.pSelection;
		}
		for (ActionProb actionProb : actionDistribution) {
			actionProb.pSelection = (sumProbability == 0.0) ? 1.0 / actionDistribution.size() : actionProb.pSelection / sumProbability;
		}
	}
	
	private AbstractGroundedAction getActionFromPolicyDistribution(List<ActionProb> actionDistribution) {
		Random random = new Random();
		double roll = random.nextDouble();
		double sumProbability = 0.0;
		for (ActionProb actionProb : actionDistribution) {
			sumProbability += actionProb.pSelection;
			if (roll < sumProbability) {
				return actionProb.ga;
			}
		}
		return null;
	}

	public boolean isFinished(State state) {
		
		boolean isFailure = (this.isFailure == null) ? false : this.isFailure.isTerminal(state);
		boolean isGoalComplete = (this.currentSubgoal == null) ? false : this.currentSubgoal.getSubgoal().goalCompleted(state);
		boolean areGoalsCompleted = this.kitchenSubdomains.size() == 1;
		return (isFailure || (isGoalComplete && areGoalsCompleted)); 
	}
	
	public boolean isSuccess(State state) {
		return this.currentRecipe.isSuccess(state);
	}
	
	public double getCostActions(List<AbstractGroundedAction> actionSequence, List<State> stateSequence) {
		double cost = 0.0;
		State previousState = this.startingState;
		for (int i = 0; i < actionSequence.size(); i++) {
			GroundedAction groundedAction = (GroundedAction)actionSequence.get(i);
			State nextState = stateSequence.get(i+1);
			cost += Human.rewardFunction.reward(previousState, groundedAction, nextState);
		
		}
		return cost;
	}
}
