package Prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class PolicyPrediction {

	public static final int DEFAULT_MAX_DEPTH = 5;
	List<KitchenSubdomain> policies;
	StateHashFactory hashingFactory;
	public PolicyPrediction(List<KitchenSubdomain> policies, StateHashFactory hashingFactory) {
		this.policies = new ArrayList<KitchenSubdomain>(policies);
		this.hashingFactory = hashingFactory;
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
		Map<StateHashTuple, Double> flowMap = new HashMap<StateHashTuple, Double>();
		return this.getFlowToStateCondition(subdomain, fromState, goalCondition, maxDepth, flowMap);
	}
	
	public double getFlowToStateCondition(KitchenSubdomain subdomain, State fromState, StateConditionTest goalCondition, int maxDepth, Map<StateHashTuple, Double> map) {
		
		
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
		double totalProb = 0.0;
		for (ActionProb actionProbability : actionDistribution) {
			double probability = actionProbability.pSelection;
			totalProb += probability;
			if (probability == 0) {
				continue;
			}
			AbstractGroundedAction ga = actionProbability.ga;
			State newState = ga.executeIn(fromState);
			
			double flow = this.getFlowToStateCondition(subdomain, newState, goalCondition, maxDepth - 1, map);
			if (flow > 0) {
				totalFlow += probability * flow;
				//System.out.println("Probability: " + probability + " flow: " + flow + " total: " + totalFlow);
			}
		}
		if (Math.abs(1.0 - totalProb) > 0.01) {
			System.err.println("Action discribution does not sum to 1.0");
		}
		if (totalFlow > 0.0) {
			//System.out.println("Depth : " + maxDepth);
			//System.out.println("Current probability: " + totalFlow);
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
		
		double sumProbability = 0.0;
		double probability = 0;
		for (int i = 0; i < this.policies.size(); i++) {
			KitchenSubdomain subdomain = this.policies.get(i);
			Policy policy = subdomain.getPolicy();
			int currentDepth = 0;
			double currentProbability = 0.0, previousProbability = 1.0;
			Map<StateHashTuple, Double> flowMap = new HashMap<StateHashTuple, Double>();
			StateHashTuple tuple = this.hashingFactory.hashState(fromState);
			flowMap.put(tuple, 1.0);
			while (currentDepth++ < maxDepth && Math.abs(currentProbability - previousProbability) > 0.001) {
				System.out.println("Depth: " + currentDepth);
				previousProbability = currentProbability;
				
				this.computeFlowToAllStates(policy, fromState, goalCondition, flowMap);
				currentProbability += this.flowT(policy, goalCondition, flowMap); // should actually have a p(T) here
				
				sumProbability += currentProbability;
				System.out.println("Current Probability: " + currentProbability);
				
			}
			PolicyProbability policyProbability = PolicyProbability.newPolicyProbability(subdomain.getPolicy(), currentProbability);
			distribution.add(policyProbability);
		}
		
		List<PolicyProbability> normalizedDistribution = new ArrayList<PolicyProbability>(distribution.size());
		
		for (PolicyProbability policyProb : distribution) {
			double normProb = (sumProbability != 0.0) ? policyProb.getProbability() / sumProbability : 0;
			PolicyProbability normalizedProb = 
					PolicyProbability.newPolicyProbability(policyProb.getPolicy(), normProb);
			normalizedDistribution.add(normalizedProb);
		}
		
		return normalizedDistribution;
	}
	
	private double flow1(Policy policy, State fromState, State nextState) {
		final State goalState = nextState.copy();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return goalState.equals(s);
			}
		};
		
		return this.flow1(policy, fromState, goalCondition);
	}
	
	private double flow1(Policy policy, State fromState, StateConditionTest goalCondition) {
		List<ActionProb> actionDistribution = policy.getActionDistributionForState(fromState);
		
		double probability = 0;
		for (ActionProb actionProbability : actionDistribution) {
			AbstractGroundedAction ga = actionProbability.ga;
			GroundedAction a = (GroundedAction)ga;
			List<TransitionProbability> transitionProbabilities = a.action.getTransitions(fromState, a.params);
			for (TransitionProbability transitionProbability : transitionProbabilities) {
				State nextState = transitionProbability.s;
				if (goalCondition.satisfies(nextState)) {
					probability += actionProbability.pSelection * transitionProbability.p;
				}
			}
		}
		return probability;
	}
	
	private double flowT(Policy policy, StateConditionTest goalCondition, Map<StateHashTuple, Double> previousFlow) {
		
		double probability = 0;
		
		for (Map.Entry<StateHashTuple, Double> entry : previousFlow.entrySet()) {
			StateHashTuple tuple = entry.getKey();
			State state = tuple.getState();
			Double fT = entry.getValue();
			double f1 = flow1(policy, state, goalCondition);
			probability += f1 * fT;
		}
		
		return probability;
	}
	
	private void computeFlowToAllStates(Policy policy, State fromState, StateConditionTest goalCondition, Map<StateHashTuple, Double> previousFlow) {
		List<ActionProb> actionDistribution = policy.getActionDistributionForState(fromState);
		
		double probability = 0;
		List<StateHashTuple> entriesToRemove = new ArrayList<StateHashTuple>();
		Map<StateHashTuple, Double> entriesToAdd = new HashMap<StateHashTuple, Double>();
		for (Map.Entry<StateHashTuple, Double> entry : previousFlow.entrySet()) {
			StateHashTuple tuple = entry.getKey();
			State previousState = tuple.getState();
			Double fT = entry.getValue();
			
			double flow = 0.0;
			for (ActionProb actionProbability : actionDistribution) {
				AbstractGroundedAction ga = actionProbability.ga;
				GroundedAction a = (GroundedAction)ga;
				List<TransitionProbability> transitionProbabilities = a.action.getTransitions(fromState, a.params);
				for (TransitionProbability transitionProbability : transitionProbabilities) {
					State nextState = transitionProbability.s;
					double f1 = flow1(policy, previousState, nextState);
					flow += f1 * fT;
					if (flow > 0.0) {
						StateHashTuple tuple2 = this.hashingFactory.hashState(nextState);
						entriesToAdd.put(tuple2, flow);
					}
				}
			}
			if (flow == 0.0) {
				entriesToRemove.add(tuple);
			}
			else {
				entry.setValue(flow);
			}
		}
		
		for (StateHashTuple tuple : entriesToRemove) {
			previousFlow.remove(tuple);
		}
		previousFlow.putAll(entriesToAdd);
	}
	
}
