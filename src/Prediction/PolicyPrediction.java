package Prediction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
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
	
	private static ActionProb getActionProbFromActionDistribution(List<ActionProb> distribution, GroundedAction observedAction) {
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
	
	public static void setSubgoal(KitchenSubdomain subdomain) {
		Domain domain = subdomain.getDomain();
		BakingSubgoal subgoal = subdomain.getSubgoal();
		IngredientRecipe ingredient = subgoal.getIngredient();
		
		List<PropositionalFunction> propFunctions = domain.getPropFunctions();
		for (PropositionalFunction pf : propFunctions) {
			((BakingPropositionalFunction)pf).changeTopLevelIngredient(ingredient);
			((BakingPropositionalFunction)pf).setSubgoal(subgoal);
		}
		
		subgoal.getGoal().changeTopLevelIngredient(ingredient);
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
			
			actionDistribution = PolicyPrediction.getActionDistributionForPolicyInState(state, subdomain);
			
			actionProbability = this.getActionProbFromActionDistribution(actionDistribution, observedAction);
			if (actionProbability != null) {
				probability = actionProbability.pSelection;
			}
			
			PolicyProbability policyProbability = 
					PolicyProbability.newPolicyProbability(subdomain, probability);
			distribution.add(policyProbability);
		}
		
		return distribution;
		
	}

	private static List<ActionProb> getActionDistributionForPolicyInState(State state, KitchenSubdomain subdomain) {
		
		PolicyPrediction.setSubgoal(subdomain);
		//List<ActionProb> actionDistribution = subdomain.getPolicy().getActionDistributionForState(state);
		Set<AbstractGroundedAction> allActions = new HashSet<AbstractGroundedAction>();
		List<ActionProb> distribution = subdomain.getPolicy().getActionDistributionForState(state);
		boolean keepGoing = true;
		while (keepGoing) {
			PolicyPrediction.setSubgoal(subdomain);
			List<ActionProb> otherDistribution = subdomain.getPolicy().getActionDistributionForState(state);
			if (!otherDistribution.retainAll(distribution)) {
				return distribution;
			}
			else {
				//System.err.println("Second call to get action distribution failed to reproduce same results");
				PolicyPrediction.setSubgoal(subdomain);
				distribution = subdomain.getPolicy().getActionDistributionForState(state);
			}
		}
		
		List<ActionProb> actionDistribution = new ArrayList<ActionProb>(allActions.size());
		double normalizedProbability = 1.0 / allActions.size();
		for (AbstractGroundedAction action : allActions) {
			actionDistribution.add(new ActionProb(action, normalizedProbability));
		}
		
		return actionDistribution;
	}

	public double getFlowToStateCondition(KitchenSubdomain subdomain, State fromState, StateConditionTest goalCondition, int maxDepth) throws InterruptedException {
		Map<StateHashTuple, Double> flowMap = new HashMap<StateHashTuple, Double>();
		return this.getFlowToStateCondition(subdomain, fromState, goalCondition, maxDepth, flowMap);
	}
	
	public double getFlowToStateCondition(KitchenSubdomain subdomain, State fromState, StateConditionTest goalCondition, int maxDepth, Map<StateHashTuple, Double> map) throws InterruptedException {
		
		
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
		
		List<ActionProb> actionDistribution = PolicyPrediction.getActionDistributionForPolicyInState(fromState, subdomain);
		
		double totalFlow = 0.0;
		double totalProb = 0.0;
		for (ActionProb actionProbability : actionDistribution) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			
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
				////System.out.println("Probability: " + probability + " flow: " + flow + " total: " + totalFlow);
			}
		}
		if (Math.abs(1.0 - totalProb) > 0.01) {
			System.err.println("Action discribution does not sum to 1.0");
		}
		if (totalFlow > 0.0) {
			////System.out.println("Depth : " + maxDepth);
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
		
		final String actualName = (actual == null) ? "*UNKNOWN*" : SubgoalDetermination.buildName(actual);
		final StateHashTuple fromStateTuple = hashingFactory.hashState(fromState);
		final StateHashFactory hashFactory = new NameDependentStateHashFactory((NameDependentStateHashFactory)hashingFactory);
		final List<KitchenSubdomain> policyDomains = this.policies;
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
					return PolicyProbabilityRunner.go(fromStateTuple, goalCondition, policy, policyDomains,
							distribution, distributionIndex, maxDepth, actualName, hashFactory, depthType);
				} 
			 });
			 futures.add(future);
		}
		//System.out.println("Threads created: " + numberThreadsCreated);
		
		double sumProbability = 0.0;
		try {
			for (Future<Double> future : futures) {
					sumProbability += future.get();	
			}
		} catch (InterruptedException | ExecutionException e) {
		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(1L, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}
		List<PolicyProbability> normalizedDistribution = new ArrayList<PolicyProbability>(distribution.size());
		
		for (PolicyProbability policyProb : distribution) {
			double normProb = (sumProbability != 0.0) ? policyProb.getProbability() / sumProbability : 1.0 / distribution.size();
			
			PolicyProbability normalizedProb = 
					PolicyProbability.updatePolicyProbability(policyProb, normProb);
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

	private static PolicyProbability getPolicyProbability(KitchenSubdomain subdomain, List<KitchenSubdomain> subdomains, 
			StateHashTuple fromStateTuple, StateConditionTest goalCondition, int maxDepth, String actualName, 
			StateHashFactory hashingFactory, int depthType) throws InterruptedException {
		
		State fromState = fromStateTuple.getState();
		
		String name = SubgoalDetermination.buildName(subdomain);
		Policy policy = subdomain.getPolicy();
		int currentDepth = 1;
		Map<StateHashTuple, Map<KitchenSubdomain, Double>> flowMap = new HashMap<StateHashTuple, Map<KitchenSubdomain, Double>>();
		
		if (actualName.equals(name)) {
			//System.out.println("");
		}
		double depthProbability = getDepthProbability(1, maxDepth, depthType);
		double currentProbability = depthProbability * flow1(subdomain, fromState, goalCondition);
		
		
		Map<KitchenSubdomain, Double> map = new HashMap<KitchenSubdomain, Double>();
		for (KitchenSubdomain policyDomain : subdomains) {
			map.put(policyDomain, 1.0);
		}
		flowMap.put(fromStateTuple, map);
		
		double totalFlow = 1.0, previousFlow = 1.0;
		while (currentDepth++ < maxDepth && totalFlow != 0) {
			
			if (subdomain.toString().equals("brownies - dry_ingredients")) {
				//System.out.println(subdomain.toString());
			}
			
			totalFlow = computeFlowToAllStates(subdomain, goalCondition, flowMap, hashingFactory);
			double flow = flowT(subdomain, goalCondition, flowMap);
			depthProbability = getDepthProbability(currentDepth, maxDepth, depthType);
			currentProbability += depthProbability * flow;
			
			if (subdomain.toString().equals("brownies - dry_ingredients")) {
//				System.out.println(subdomain.toString());
//				System.out.println("Depth: " + currentDepth);
//				System.out.println("Flow map size: " + flowMap.size());
//				
//				System.out.println("Flow: " + flow);
//				System.out.println("Depth Prob: " + depthProbability);
//				System.out.println("Current Prob: " + currentProbability);
				
				
				
			}
			
			if (flow > 1.0) {
				//System.err.println("flow: " + totalFlow);
			}

			if (currentProbability > 1.0) {
				//System.err.println("Probability is " + currentProbability);
			}
			
			if (subdomain.toString().equals("brownies - dry_ingredients")) {
				//System.out.println();
			}
			
		}
		if (actualName.equals(name) && currentProbability == 0.0) {
			//System.err.println("Probability of actual is: " + currentProbability);
		}
		
		
		
		return PolicyProbability.newPolicyProbability(subdomain, currentProbability);
	}
	
	private static double flow1(KitchenSubdomain policy, State fromState, State nextState) throws InterruptedException {
		final State goalState = nextState.copy();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return goalState.equals(s);
			}
		};
		
		return flow1(policy, fromState, goalCondition);
	}
	
	private static double flow1(KitchenSubdomain policy, State fromState, StateConditionTest goalCondition) throws InterruptedException {
		// Get possible actions from the fromState
		List<ActionProb> actionDistribution = PolicyPrediction.getActionDistributionForPolicyInState(fromState, policy);
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		
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
	
	private static double flowT(KitchenSubdomain policy, StateConditionTest goalCondition, Map<StateHashTuple, Map<KitchenSubdomain,  Double>> previousFlow) throws InterruptedException {
		
		// Setup for computation
		double probability = 0;
		double probabilityStaying = 0.9;
		// For each state that we've calculated flow up to T-1
		for (Map.Entry<StateHashTuple, Map<KitchenSubdomain, Double>> entry : previousFlow.entrySet()) {
			Map<KitchenSubdomain,Double> policyFlowMap = entry.getValue();
			// Get state hash tuple, and state
			StateHashTuple previousStateTuple = entry.getKey();
			State previousState = previousStateTuple.getState();
			int normalizer = policyFlowMap.size() - 1;
			double probabilitySwitching = (normalizer == 0) ? 0.0 : 1.0 / normalizer;
			
			double fT = 0;
			
			// Get flow_1 from this state to the goal state
			double f1 = flow1(policy, previousState, goalCondition);
			
			for (Map.Entry<KitchenSubdomain, Double> entry2 : policyFlowMap.entrySet()) {
				KitchenSubdomain otherPolicy = entry2.getKey();
				Double policyFT = entry2.getValue();
				
				// Get flow_{T-1} from start up to this state
				double transitionProbability = (policy == otherPolicy) ? probabilityStaying : probabilitySwitching;
				fT += transitionProbability * policyFT;
			}
			// Update total probability
			probability += f1 * fT;
			
		}
		
		return probability;
	}
	
	private static double computeFlowToAllStates(KitchenSubdomain policy, StateConditionTest goalCondition, Map<StateHashTuple, Map<KitchenSubdomain, Double>> previousFlow, StateHashFactory hashingFactory) {
		
		if (policy.toString().equals("brownies - dry_ingredients")) {
			//System.out.println("Previous Flow: " + previousFlow.size());
		}
		
		// Map that tracks flow from all previous states to all next states
		Map<StateHashTuple, Map<KitchenSubdomain, Map<StateHashTuple, Double>>> flowStateToState = 
				updateFlowStateToState(policy, previousFlow, hashingFactory);
		
		if (policy.toString().equals("brownies - dry_ingredients")) {
			//System.out.println("FlowState-State: " + flowStateToState.size());
		}
		
		// Clear previous flow values
		previousFlow.clear();
		
		double totalFlowToAllStates = updatePreviousFlow(previousFlow, flowStateToState);
		
		if (policy.toString().equals("brownies - dry_ingredients")) {
			//System.out.println("Updated Flow: " + previousFlow.size());
		}
		
		return totalFlowToAllStates;
	}

	private static double updatePreviousFlow(
			Map<StateHashTuple, Map<KitchenSubdomain, Double>> previousFlow,
			Map<StateHashTuple, Map<KitchenSubdomain, Map<StateHashTuple, Double>>> flowStateToState) {
		double totalFlowToAllStates = 0.0;
		StateHashTuple previousStateTuple;
		StateHashTuple nextStateTuple;
		double fT;
		Map<KitchenSubdomain, Map<StateHashTuple, Double>> entryMap;
		// Iterate over all previousState maps
		for (Map.Entry<StateHashTuple, Map<KitchenSubdomain, Map<StateHashTuple, Double>>> entry : flowStateToState.entrySet()) {
			
			// Setup items for computation
			entryMap = entry.getValue();
			previousStateTuple = entry.getKey();
			int normalizer = entryMap.size() - 1;
			double probabilitySwitching = (normalizer == 0) ? 0.0 : 1.0 / normalizer;
			
			// Iterate over all nextState maps
			for (Map.Entry<KitchenSubdomain, Map<StateHashTuple, Double>> entry2 : entryMap.entrySet()) {
				
				KitchenSubdomain policy = entry2.getKey();
				Map<StateHashTuple, Double> flowPreviousToNext = entry2.getValue();
				
				for (Map.Entry<StateHashTuple, Double> entry3 : flowPreviousToNext.entrySet()) {
					// Setup items for computation
					nextStateTuple = entry3.getKey();
					fT = entry3.getValue();
					
					// Get current flow to this next state
					Map<KitchenSubdomain, Double> previousPolicyFlow = previousFlow.get(nextStateTuple);
					if (previousPolicyFlow == null) {
						previousPolicyFlow = new HashMap<KitchenSubdomain, Double>();
						previousFlow.put(nextStateTuple, previousPolicyFlow);
					}
					Double currentValue = previousPolicyFlow.get(policy);
					
					// Add new value to this next state flow
					Double updatedValue = (currentValue == null) ? fT : fT + currentValue;
					totalFlowToAllStates += fT;
					
					// Store this new value
					previousPolicyFlow.put(policy, updatedValue);
				}
				
			}
		}
		return totalFlowToAllStates;
	}

	private static Map<StateHashTuple, Map<KitchenSubdomain, Map<StateHashTuple, Double>>> updateFlowStateToState(KitchenSubdomain policy,
			Map<StateHashTuple, Map<KitchenSubdomain, Double>> previousFlow,
			StateHashFactory hashingFactory) {
		
		Map<StateHashTuple, Map<KitchenSubdomain, Map<StateHashTuple, Double>>> flowStateToState = 
				new HashMap<StateHashTuple, Map<KitchenSubdomain, Map<StateHashTuple, Double>>>();
		
		List<ActionProb> actionDistribution;
		StateHashTuple previousStateTuple;
		State previousState;
		Double previousStateFlow;
		Map<KitchenSubdomain, Map<StateHashTuple, Double>> flowPolicyToState;
		Map<StateHashTuple, Double> flowPreviousToState;
		
		
		// For every state in previous flow
		for (Map.Entry<StateHashTuple, Map<KitchenSubdomain, Double>> entry : previousFlow.entrySet()) {
			
			// Get items necessary for computation
			previousStateTuple = entry.getKey();
			previousState = previousStateTuple.getState();
			Map<KitchenSubdomain, Double> policyFlowMap = entry.getValue();
			
			flowPolicyToState = flowStateToState.get(previousStateTuple);
			
			if (flowPolicyToState == null) {
				flowPolicyToState = new HashMap<KitchenSubdomain, Map<StateHashTuple, Double>>();
				flowStateToState.put(previousStateTuple, flowPolicyToState);
			}
			
			
			
			// For every policy in policy flow map
			for (Map.Entry<KitchenSubdomain, Double> entry2 : policyFlowMap.entrySet()) {
				
				// Get items for computation
				previousStateFlow = entry2.getValue();
				KitchenSubdomain otherPolicy = entry2.getKey();
				
				flowPreviousToState = flowPolicyToState.get(otherPolicy);
				if (flowPreviousToState == null) {
					flowPreviousToState = new HashMap<StateHashTuple, Double>();
					flowPolicyToState.put(otherPolicy, flowPreviousToState);
				}
				/*
				if (policy.toString().equals("brownies - dry_ingredients")) {
					if (otherPolicy.toString().equals(policy.toString())) {
						System.out.print("");
					}
				}*/
				
				// Get all actions with positive policy probability available from this state
				actionDistribution = PolicyPrediction.getActionDistributionForPolicyInState(previousState, otherPolicy);
				
				
				
				if (policy.toString().equals("brownies - dry_ingredients")) {
					if (otherPolicy.toString().equals(policy.toString())) {
						//System.out.print("");
					}
					//System.out.println(otherPolicy.toString());
					//System.out.println("Num actions in state: " + actionDistribution.size());
					for (ActionProb actionProb : actionDistribution) {
						//System.out.println("\t" + actionProb.toString());
					}
					
				}
				
				PolicyPrediction.updateFlowPreviousToNext(hashingFactory, previousState, previousStateFlow, actionDistribution, flowPreviousToState);
			}
			
			if (policy.toString().equals("brownies - dry_ingredients")) {
				//System.out.println("Num policy entries: " + flowPolicyToState.size());
			}
			
		}
		return flowStateToState;
	}

	private static void updateFlowPreviousToNext(
			StateHashFactory hashingFactory, State previousState,
			Double previousStateFlow, List<ActionProb> actionDistribution,
			Map<StateHashTuple, Double> flowPreviousToState) {
		
		StateHashTuple nextStateTuple;
		State nextState;
		GroundedAction groundedAction;
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
	
	private static class PolicyProbabilityRunner {
		public static double go(StateHashTuple fromStateTuple, StateConditionTest goalCondition, 
				KitchenSubdomain policy, List<KitchenSubdomain> policyDomains, 
				List<PolicyProbability> distribution, int distributionIndex, int maxDepth, 
				String actualName, StateHashFactory hashingFactory, int depthType) throws InterruptedException {
			
			PolicyProbability probability = getPolicyProbability(policy, policyDomains, fromStateTuple, goalCondition,
					maxDepth, actualName, hashingFactory, depthType);
			
			distribution.set(distributionIndex, probability);
			return probability.getProbability();
		}
	}
	
}
