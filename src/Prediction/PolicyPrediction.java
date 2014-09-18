package Prediction;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class PolicyPrediction {

	public static final int DEFAULT_MAX_DEPTH = 5;
	List<KitchenSubdomain> policies;
	public PolicyPrediction(List<KitchenSubdomain> policies) {
		this.policies = new ArrayList<KitchenSubdomain>(policies);
	}
	
	private ActionProb getActionProbFromActionDistribution(List<ActionProb> distribution, GroundedAction observedAction) {
		ActionProb foundActionProbability = null;
		for (ActionProb actionProbability : distribution) {
			GroundedAction action = (GroundedAction)actionProbability.ga;
			if (action.equals(observedAction)) {
				foundActionProbability = actionProbability;
				break;
			}
			
		}
		return foundActionProbability;
	}
	
	public List<PolicyProbability> getPolicyDistributionFromStateActionPair(State state, GroundedAction observedAction) {
		List<PolicyProbability> distribution = new ArrayList<PolicyProbability>();
		
		Policy policy;
		List<ActionProb> actionDistribution;
		ActionProb actionProbability;
		double probability = 0;
		for (int i = 0; i < this.policies.size(); i++) {
			KitchenSubdomain subdomain = this.policies.get(i);
			policy = subdomain.getPolicy();
			actionDistribution = policy.getActionDistributionForState(state);
			
			actionProbability = this.getActionProbFromActionDistribution(actionDistribution, observedAction);
			if (actionProbability != null) {
				probability = actionProbability.pSelection;
			}
			
			PolicyProbability policyProbability = PolicyProbability.newPolicyProbability(policy, probability);
			distribution.add(policyProbability);
		}
		
		return distribution;
		
	}

	public double getFlowToStateCondition(KitchenSubdomain subdomain, State fromState, StateConditionTest goalCondition, int maxDepth) {
		
		
		BakingSubgoal subgoal = subdomain.getSubgoal();
		Domain domain = subdomain.getDomain();
		Recipe recipe = subdomain.getRecipe();
		BakingPropositionalFunction isFailure = recipe.getFailurePF(domain);
		
		for (GroundedProp failurePF : isFailure.getAllGroundedPropsForState(fromState)) {
			if (failurePF.isTrue(fromState)) {
				return 0.0;
			}
		}

		if (!subgoal.allPreconditionsCompleted(fromState)) {
			return 0.0;
		}
		
		Policy policy = subdomain.getPolicy();
		
		if (goalCondition.satisfies(fromState)) {
			return 1.0;
		}
		if (maxDepth == 0) {
			return 0.0;
		}
		
		List<ActionProb> actionDistribution = policy.getActionDistributionForState(fromState);
		
		double totalFlow = 0.0;
		for (ActionProb actionProbability : actionDistribution) {
			AbstractGroundedAction ga = actionProbability.ga;
			State newState = ga.executeIn(fromState);
			
			double probability = actionProbability.pSelection;
			totalFlow += probability * this.getFlowToStateCondition(subdomain, newState, goalCondition, maxDepth - 1);
		}
		if (totalFlow > 0.0) {
			System.out.println("Depth : " + maxDepth);
			System.out.println("Current probability: " + totalFlow);
		}
		return totalFlow;
	}
	
	public List<PolicyProbability> getPolicyDistributionFromStatePair(State fromState, State endState) {
		return this.getPolicyDistributionFromStatePair(fromState, endState, DEFAULT_MAX_DEPTH);
	}
	
	public List<PolicyProbability> getPolicyDistributionFromStatePair(State fromState, State endState, int maxDepth) {
		final State goalState = endState.copy();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return goalState.equals(s);
			}
		};
		
		return this.getPolicyDistributionFromStateGoalCondition(fromState, goalCondition, maxDepth);
	}
	
	public List<PolicyProbability> getPolicyDistributionFromStateGoalCondition(State fromState, StateConditionTest goalCondition) {
		return this.getPolicyDistributionFromStateGoalCondition(fromState, goalCondition, DEFAULT_MAX_DEPTH);
	}
	
	public List<PolicyProbability> getPolicyDistributionFromStateGoalCondition(State fromState, StateConditionTest goalCondition, int maxDepth) {
		List<PolicyProbability> distribution = new ArrayList<PolicyProbability>();
		
		Policy policy;
		double probability = 0;
		for (int i = 0; i < this.policies.size(); i++) {
			System.out.println("Policy " + i);
			KitchenSubdomain subdomain = this.policies.get(i);
			probability = this.getFlowToStateCondition(subdomain, fromState, goalCondition, maxDepth);
			
			PolicyProbability policyProbability = PolicyProbability.newPolicyProbability(subdomain.getPolicy(), probability);
			distribution.add(policyProbability);
		}
		
		return distribution;
	}
	
	
	
	
}
