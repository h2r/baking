package Prediction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.statehashing.ImmutableNameDependentStateHashFactory;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
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
import edu.brown.cs.h2r.baking.Experiments.SubgoalDetermination;
import edu.brown.cs.h2r.baking.PropositionalFunctions.BakingPropositionalFunction;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class PolicyPrediction {

	private static int numberThreadsCreated = 0;
	public static final int DEFAULT_MAX_DEPTH = 5;
	private int nextPolicy = 0;
	private int depthType;
	List<KitchenSubdomain> policies;
	
	public PolicyPrediction(List<KitchenSubdomain> policies, int depthType) {
		this.policies = new ArrayList<KitchenSubdomain>(policies);
		this.depthType = depthType;
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
	
	/*public List<PolicyProbability> getPolicyDistributionFromStatePair(State fromState, State endState) {
		return this.getPolicyDistributionFromStatePair(fromState, endState, DEFAULT_MAX_DEPTH);
	}*/
	
	public List<PolicyProbability> getPolicyDistributionFromStatePair(State fromState, State endState, int maxDepth, KitchenSubdomain actual, StateHashFactory hashingFactory, int depthType) {
		final State goalState = endState.copy();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return goalState.equals(s);
			}
		};
		
		return this.getPolicyDistributionFromStateGoalCondition(fromState, goalCondition, maxDepth, actual, hashingFactory, depthType);
	}
	
	/*public List<PolicyProbability> getPolicyDistributionFromStateGoalCondition(State fromState, StateConditionTest goalCondition) {
		return this.getPolicyDistributionFromStateGoalCondition(fromState, goalCondition, DEFAULT_MAX_DEPTH);
	}*/
	
	public List<PolicyProbability> getPolicyDistributionFromStateGoalCondition(State fromState, 
			final StateConditionTest goalCondition, final int maxDepth, KitchenSubdomain actual, StateHashFactory hashingFactory, final int depthType){
		
		final List<PolicyProbability> distribution = 
				new ArrayList<PolicyProbability>(Collections.nCopies(this.policies.size(), new PolicyProbability()));
		
		final String actualName = SubgoalDetermination.buildName(actual);
		final StateHashTuple fromStateTuple = hashingFactory.hashState(fromState);
		final StateHashFactory hashFactory = new NameDependentStateHashFactory((NameDependentStateHashFactory)hashingFactory);
		int numCores = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numCores);
		List<Future<Double>> futures  = new LinkedList<Future<Double>>();
		this.nextPolicy = 0;
		for (int i = 0; i < this.policies.size(); i++) {
			final KitchenSubdomain policy = this.policies.get(i);
			final int distributionIndex = i;
			// TODO can call class with explicit policy, don't need to access a shared resource
			Future<Double> future = executor.submit(new Callable<Double>() {
				@Override
				public Double call() throws Exception {
					return PolicyProbabilityRunner.go(fromStateTuple, goalCondition, policy, 
							distribution, distributionIndex, maxDepth, actualName, hashFactory, depthType);
				} 
			 });
			 futures.add(future);
		}
		System.out.println("Threads created: " + numberThreadsCreated);
		
		double sumProbability = 0.0;
		try {
			for (Future<Double> future : futures) {
					sumProbability += future.get();	
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			executor.shutdownNow();
			throw new RuntimeException(e);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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
	
	// 0 uniform
	// 1 descending weight
	// 2 ascending weight
	public static double getDepthProbability(int depth, int maxDepth, int depthType) {
		if (depthType == 0) {
			return 1.0 / maxDepth;
		} else if (depthType == 1) {
			double numerator = (maxDepth + 1) - depth;
			double denominator = (maxDepth + 1) * maxDepth / 2.0;
			return numerator / denominator;
		} else if (depthType == 2) {
			double numerator = depth;
			double denominator = (maxDepth + 1) * maxDepth / 2.0;
			return numerator / denominator;
		}
		return 0.0;
	}

	private static PolicyProbability getPolicyProbability(KitchenSubdomain subdomain, StateHashTuple fromStateTuple,
			StateConditionTest goalCondition, int maxDepth, String actualName, StateHashFactory hashingFactory, int depthType) {
		
		State fromState = fromStateTuple.getState();
		
		String name = SubgoalDetermination.buildName(subdomain);
		Policy policy = subdomain.getPolicy();
		int currentDepth = 0;
		Map<StateHashTuple, Double> flowMap = new HashMap<StateHashTuple, Double>();
		
		if (actualName.equals(name)) {
			System.out.println("");
		}
		double depthProbability = getDepthProbability(1, maxDepth, depthType);
		
		
		double currentProbability = depthProbability * flow1(policy, fromState, goalCondition);
		flowMap.put(fromStateTuple, 1.0);
		
		double totalFlow = 1.0, previousFlow = 1.0;
		while (currentDepth++ < maxDepth && totalFlow != 0) {
			SubgoalDetermination.setSubgoal(subdomain);
			totalFlow = computeFlowToAllStates(policy, goalCondition, flowMap, hashingFactory);
			double flow = flowT(policy, goalCondition, flowMap);
			depthProbability = getDepthProbability(currentDepth, maxDepth, depthType);
			currentProbability += depthProbability * flow;
			
			if (flow > 1.0) {
				System.err.println("flow: " + totalFlow);
			}

			if (currentProbability > 1.0) {
				System.err.println("Probability is " + currentProbability);
			}
			
		}
		if (actualName.equals(name) && currentProbability == 0.0) {
			//System.err.println("Probability of actual is: " + currentProbability);
		}
		
		return PolicyProbability.newPolicyProbability(subdomain.getPolicy(), currentProbability);
	}
	
	private static double flow1(Policy policy, State fromState, State nextState) {
		final State goalState = nextState.copy();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return goalState.equals(s);
			}
		};
		
		return flow1(policy, fromState, goalCondition);
	}
	
	private static double flow1(Policy policy, State fromState, StateConditionTest goalCondition) {
		// Get possible actions from the fromState
		List<ActionProb> actionDistribution = policy.getActionDistributionForState(fromState);
		
		// Setup for computation
		GroundedAction groundedAction;
		double probability = 0;
		State nextState;
		
		// Iterate through each action
		for (ActionProb actionProbability : actionDistribution) {
			
			// Setup for computation
			groundedAction = (GroundedAction)actionProbability.ga;
			
			// Get all possible transitions from the fromState taking this grounded action
			List<TransitionProbability> transitionProbabilities = 
					groundedAction.action.getTransitions(fromState, groundedAction.params);
			
			// Iterate through all possible transitions
			for (TransitionProbability transitionProbability : transitionProbabilities) {
				
				// Setup for computation
				nextState = transitionProbability.s;
				
				// If and only if the next state satisfies the goal condition, do we add in the transition probability
				if (goalCondition.satisfies(nextState)) {
					probability += actionProbability.pSelection * transitionProbability.p;
				}
			}
		}
		return probability;
	}
	
	private static double flowT(Policy policy, StateConditionTest goalCondition, Map<StateHashTuple, Double> previousFlow) {
		
		// Setup for computation
		double probability = 0;
		
		// For each state that we've calculated flow up to T-1
		for (Map.Entry<StateHashTuple, Double> entry : previousFlow.entrySet()) {
			
			// Get state hash tuple, and state
			StateHashTuple previousStateTuple = entry.getKey();
			State previousState = previousStateTuple.getState();
			
			// Get flow_{T-1} from start up to this state
			Double fT = entry.getValue();
			
			// Get flow_1 from this state to the goal state
			double f1 = flow1(policy, previousState, goalCondition);
			
			// Update total probability
			probability += f1 * fT;
		}
		
		return probability;
	}
	
	private static double computeFlowToAllStates(Policy policy, StateConditionTest goalCondition, Map<StateHashTuple, Double> previousFlow, StateHashFactory hashingFactory) {
		
		// Setup computation
		List<ActionProb> actionDistribution;
		StateHashTuple previousStateTuple, nextStateTuple;
		State previousState, nextState;
		Double previousStateFlow;
		double f1;
		GroundedAction groundedAction;
		
		// Map that tracks flow from all previous states to all next states
		Map<StateHashTuple, Map<StateHashTuple, Double>> flowStateToState = 
				new HashMap<StateHashTuple, Map<StateHashTuple, Double>>();
		Map<StateHashTuple, Double> flowPreviousToState;
		
		// For every state in previous flow
		for (Map.Entry<StateHashTuple, Double> entry : previousFlow.entrySet()) {
			
			// Get items necessary for computation
			previousStateTuple = entry.getKey();
			previousState = previousStateTuple.getState();
			previousStateFlow = entry.getValue();
			
			// Get flow from previous state to state map
			flowPreviousToState = flowStateToState.get(previousStateTuple);
			
			// Check that the flowMap contains previousStateTuple
			if (flowPreviousToState == null) {
				flowPreviousToState = new HashMap<StateHashTuple, Double>();
				flowStateToState.put(previousStateTuple, flowPreviousToState);
			}
			
			// Get all actions with positive policy probability available from this state
			actionDistribution = policy.getActionDistributionForState(previousState);
			
			// For all actions
			for (ActionProb actionProbability : actionDistribution) {
				
				// Get grounded action
				groundedAction = (GroundedAction)actionProbability.ga;
				
				// Get all possible transitions from previousState given this grounded action
				List<TransitionProbability> transitionProbabilities = 
						groundedAction.action.getTransitions(previousState, groundedAction.params);
				
				// For all possible transitions
				for (TransitionProbability transitionProbability : transitionProbabilities) {
					
					// Get state
					nextState = transitionProbability.s;
					
					// Hash next state
					nextStateTuple = hashingFactory.hashState(nextState);
					
					// Get the previous flow to the next state
					Double flowPreviousToNext = flowPreviousToState.get(nextStateTuple);
					
					// Calculate update
					double update = transitionProbability.p * actionProbability.pSelection * previousStateFlow;
					
					// Update flow
					flowPreviousToNext = (flowPreviousToNext == null) ? update : flowPreviousToNext + update;
					
					// Store result
					flowPreviousToState.put(nextStateTuple, flowPreviousToNext);
				}
			}
		}
		
		// Clear previous flow values
		previousFlow.clear();
		
		Map<StateHashTuple, Double> entryMap;
		double totalFlowToAllStates = 0.0;
		
		// Iterate over all previousState maps
		for (Map.Entry<StateHashTuple, Map<StateHashTuple, Double>> entry : flowStateToState.entrySet()) {
			
			// Setup items for computation
			entryMap = entry.getValue();
			previousStateTuple = entry.getKey();
			
			// Iterate over all nextState maps
			for (Map.Entry<StateHashTuple, Double> entry2 : entryMap.entrySet()) {
				
				// Setup items for computation
				nextStateTuple = entry2.getKey();
				f1 = entry2.getValue();
				
				// Get current flow to this next state
				Double currentValue = previousFlow.get(nextStateTuple);
				
				// Add new value to this next state flow
				Double updatedValue = (currentValue == null) ? f1 : f1 + currentValue;
				totalFlowToAllStates += f1;
				// Store this new value
				previousFlow.put(nextStateTuple, updatedValue);
			}
		}
		return totalFlowToAllStates;
	}
	
	private static class PolicyProbabilityRunner {
		public static double go(StateHashTuple fromStateTuple, StateConditionTest goalCondition, 
				KitchenSubdomain policy, List<PolicyProbability> distribution, int distributionIndex, int maxDepth, 
				String actualName, StateHashFactory hashingFactory, int depthType) {
			PolicyProbability probability = getPolicyProbability(policy, fromStateTuple, goalCondition,
					maxDepth, actualName, hashingFactory, depthType);
			distribution.set(distributionIndex, probability);
			return probability.getProbability();
		}
	}
	
}
