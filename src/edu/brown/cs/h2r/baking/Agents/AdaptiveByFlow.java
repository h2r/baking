package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;

public class AdaptiveByFlow extends AdaptiveAgent{
	private static final StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	private static final int MAX_ALPHA = 3;
	
	private PolicyPrediction prediction;
	public AdaptiveByFlow(Domain domain, boolean isRobot, ActionTimeGenerator timeGenerator, List<Recipe> recipes, boolean useScheduling) {
		super("partner", isRobot, domain, timeGenerator, recipes, useScheduling);
	}
	
	protected AdaptiveByFlow(Domain domain, Map<String, Object> objectMap, ActionTimeGenerator timeGenerator, List<Recipe> recipes, State startState) {
		super(domain, objectMap, timeGenerator, recipes, startState);
	}
	
	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();
		return map;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " scheduling: " + this.useScheduling;
	}
	
	@Override
	protected void init() {
		this.prediction = new PolicyPrediction(this.subdomains);
	}
	
	@Override
	protected final List<PolicyProbability> getPolicyDistribution(State currentState, GroundedAction agentsAction) {
		
		if (this.stateHistory.size() < 2) {
			return this.prediction.getUniformPolicyDistribution();
		}
		State lastObservedState = this.stateHistory.get(this.stateHistory.size() - 2);
		if (agentsAction != null && agentsAction.action != null) {
			lastObservedState = this.lastAction.executeIn(lastObservedState);
		}
		
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
		
		
		AbstractGroundedAction chosenAction = bestPolicy.getPolicyDomain().getAction(state);
		return chosenAction;
	}
	
	@Override
	protected double getTransitionProbability(Policy from, Policy to) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
