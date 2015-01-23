package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Prediction.PolicyPrediction;
import edu.brown.cs.h2r.baking.Prediction.PolicyProbability;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;

public class AdaptiveByFlow extends AdaptiveAgent implements Agent {
	private static final StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	private static final int MAX_ALPHA = 5;
	private PolicyPrediction prediction;
	public AdaptiveByFlow(Domain domain, ActionTimeGenerator timeGenerator, boolean useScheduling) {
		super(domain, timeGenerator, useScheduling);
	}
	
	@Override
	protected void init() {
		this.prediction = new PolicyPrediction(this.subdomains);
	}
	
	@Override
	protected final List<PolicyProbability> getPolicyDistribution(State currentState) {
		
		State lastObservedState = this.stateHistory.get(this.stateHistory.size() - 1);
		if (currentState.equals(lastObservedState)) {
			return null;
		} else {
			//System.err.println("Not equal");
		}
		List<PolicyProbability> policyDistribution = 
				prediction.getPolicyDistributionFromStatePair(lastObservedState, currentState, 
						MAX_ALPHA, null, null, AdaptiveByFlow.hashingFactory, 0);
		this.stateHistory.add(currentState);
		
		return policyDistribution;
	}

	@Override
	protected AbstractGroundedAction getActionFromPolicyDistribution(List<PolicyProbability> policyDistribution, State state) {
		if (policyDistribution.size() == 0) {
			//System.err.println("No valid policies were found. Returning null action");
			return null;
		}
		
		List<PolicyProbability> bestPolicies = new ArrayList<PolicyProbability>();
		double maxProbability = 0.0;
		for (PolicyProbability policy : policyDistribution) {
			double policyProbability = policy.getProbability();
			if (policyProbability > maxProbability) {
				bestPolicies.clear();
				bestPolicies.add(policy);
				maxProbability = policyProbability;
			}
			else if (policyProbability == maxProbability) {
				bestPolicies.add(policy);
			}
		}
		
		Collections.shuffle(bestPolicies);
		PolicyProbability bestPolicy = bestPolicies.get(0);
		//System.out.println("Inferred subgoal " + bestPolicy.toString());
		TerminalFunction terminalFunction = bestPolicy.getPolicyDomain().getTerminalFunction();
		//if (terminalFunction.isTerminal(state)) {
		//	return null;
		//}
		
		
		AbstractGroundedAction chosenAction = 
				this.getAgentsActionFromPolicy(bestPolicy.getPolicyDomain().getPolicy(), state, terminalFunction);
		return chosenAction;
	}
	
	
	
	private final AbstractGroundedAction getAgentsActionFromPolicy(Policy policy, State state, TerminalFunction terminalFunction) {
		
		boolean isValidAction = false;
		AbstractGroundedAction action = policy.getAction(state);
		int tries = 0;
		while (!isValidAction) {
			if (Thread.interrupted()) {
				return null;
			}
			if (action == null){
				return null;
			}
			state = action.executeIn(state);
			if (terminalFunction.isTerminal(state)) {
				return null;
			}
			action = policy.getAction(state);
			
			if (action == null) {
				return null;
			}
			
			GroundedAction groundedAction = (GroundedAction)action;
			groundedAction.params[0] = this.getAgentName();
			
			BakingAction bakingAction = (BakingAction)groundedAction.action;
			BakingActionResult result = bakingAction.checkActionIsApplicableInState(state, groundedAction.params);
			
			if (!result.getIsSuccess()) {
				System.err.println(this.getAgentName() + " chose action " + groundedAction.toString() + " which cannot succeed because:");
				System.err.println(result.getWhyFailed());
				tries++;
				if (tries == 3) {
					return null;
				}
			}
			
			isValidAction = (
					this.canAgentGo(groundedAction, state) && 
					groundedAction.action.applicableInState(state, action.params));
		}
		
		return action;
	}
	
	private final boolean canAgentGo(GroundedAction action, State state) {
		return (action.params[0].equals(this.getAgentName()));	
	}

	public final String getAgentName() {
		return "partner";
	}
	
	@Override
	protected double getTransitionProbability(Policy from, Policy to) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
