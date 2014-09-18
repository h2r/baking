package Prediction;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;

public class PolicyPrediction {

	public static final int DEFAULT_MAX_DEPTH = 5;
	List<Policy> policies;
	public PolicyPrediction(List<Policy> policies) {
		this.policies = new ArrayList<Policy>(policies);
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
			policy = this.policies.get(i);
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

	public double getFlowToStateCondition(Policy policy, State fromState, StateConditionTest goalCondition, int maxDepth) {
		
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
			totalFlow += probability * this.getFlowToStateCondition(policy, newState, goalCondition, maxDepth - 1);
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
			policy = this.policies.get(i);
			probability = this.getFlowToStateCondition(policy, fromState, goalCondition, maxDepth);
			
			PolicyProbability policyProbability = PolicyProbability.newPolicyProbability(policy, probability);
			distribution.add(policyProbability);
		}
		
		return distribution;
	}
	
	
	
	
}
