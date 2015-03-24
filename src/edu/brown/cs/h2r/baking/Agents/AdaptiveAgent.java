package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.common.StateYAMLParser;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.parallel.Parallel;
import burlap.parallel.Parallel.ForEachCallable;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.Prediction.PolicyProbability;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Assignment;
import edu.brown.cs.h2r.baking.Scheduling.Assignments;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveStarScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.actions.ResetAction;

public abstract class AdaptiveAgent extends Agent{
	private final Domain domain;
	protected final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	
	protected final ActionTimeGenerator timeGenerator;
	
	protected final boolean useScheduling;
	
	protected final static RewardFunction rewardFunction = new RewardFunction() {
	
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return (a.action instanceof ResetAction) ? -2 : -1;
		}
		
	};
	
	protected final List<State> stateHistory;
	
	private final List<PolicyProbability> policyBeliefDistribution;
	
	protected final List<KitchenSubdomain> subdomains;
	
	protected final List<Recipe> recipes;
	
	public AdaptiveAgent(String name, boolean isRobot, Domain domain, ActionTimeGenerator timeScheduler, List<Recipe> recipes, boolean useScheduling) {
		super(name, isRobot);
		this.domain = domain;
		this.stateHistory = new ArrayList<State>();
		this.subdomains = new ArrayList<KitchenSubdomain>();
		this.policyBeliefDistribution = new ArrayList<PolicyProbability>();
		this.timeGenerator = timeScheduler;
		this.useScheduling = useScheduling;
		this.recipes = recipes;
	}
	
	protected AdaptiveAgent(Domain domain, Map<String, Object> objectMap, ActionTimeGenerator timeGenerator, List<Recipe> recipes, State startState) {
		super(objectMap);
		this.domain = domain;
		this.stateHistory = new ArrayList<State>();
		this.subdomains = new ArrayList<KitchenSubdomain>();
		this.policyBeliefDistribution = new ArrayList<PolicyProbability>();
		this.recipes = recipes;
		this.timeGenerator = timeGenerator;
		this.useScheduling = (Boolean)objectMap.get("use_scheduling");
		Map<String, Double> policyDistribution = (Map<String, Double>)objectMap.get("policy_distribution");
		this.setInitialState(startState);
		List<PolicyProbability> newDistribution = new ArrayList<PolicyProbability>();
		for (PolicyProbability prob : this.policyBeliefDistribution){
			Double value = policyDistribution.get(prob.getPolicyDomain().toString());
			newDistribution.add(PolicyProbability.newPolicyProbability(prob.getPolicyDomain(), value));
		}
		this.policyBeliefDistribution.clear();
		this.policyBeliefDistribution.addAll(newDistribution);
		
		List<String> actionParams = (List<String>)objectMap.get("last_action");
		if (actionParams != null) {
			Action action = (actionParams.get(0).equals("null")) ? null : domain.getAction(actionParams.get(0));
			String[] params = actionParams.subList(1, actionParams.size()).toArray(new String[actionParams.size()-1]);
			this.lastAction = new GroundedAction(action, params);
		}
		StateYAMLParser parser = new StateYAMLParser(domain, hashingFactory);
		List<String> stateHistory = (List<String>)objectMap.get("state_history");
		for (String str : stateHistory) {
			this.stateHistory.add(parser.stringToState(str));
		}
	}
	
	@Override
	protected Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();
		map.put("use_scheduling", this.useScheduling);
		Map<String, Double> policyDistribution = new HashMap<String, Double>();
		for (PolicyProbability prob : this.policyBeliefDistribution) {
			policyDistribution.put(prob.getPolicyDomain().toString(), prob.getProbability());
		}
		map.put("policy_distribution", policyDistribution);
		StateYAMLParser parser = new StateYAMLParser(this.domain, hashingFactory);
		List<String> stateHistory = new ArrayList<String>();
		for (State state : this.stateHistory) {
			stateHistory.add(parser.stateToString(state));
		}
		map.put("state_history", stateHistory);
		if (this.lastAction != null) {
			List<String> actionParams = new ArrayList<String>(Arrays.asList(this.lastAction.params));
			String actionName = (this.lastAction.action == null) ? "null" : this.lastAction.actionName();
			actionParams.add(0, actionName);
			map.put("last_action", actionParams);
		}
		return map;
	}
	
	@Override
	public void setInitialState(State state) {
		super.setInitialState(state);
		this.stateHistory.clear();
		this.subdomains.clear();
		this.policyBeliefDistribution.clear();
		this.stateHistory.add(state);
		List<KitchenSubdomain> subdomains = AgentHelper.generateAllRTDPPoliciesParallel(domain, state, this.recipes,
				AdaptiveAgent.rewardFunction ,AdaptiveAgent.hashingFactory);
		this.subdomains.addAll(subdomains);
		this.policyBeliefDistribution.addAll(this.getInitialPolicyDistribution(subdomains));
		this.init();
	}
	
	@Override
	public void reset() {
		State first = this.stateHistory.get(0);
		this.stateHistory.clear();
		this.stateHistory.add(first);
		this.policyBeliefDistribution.clear();
		this.policyBeliefDistribution.addAll(this.getInitialPolicyDistribution(subdomains));
	}
	
	@Override
	public void performResetAction() {
		this.reset();
	}
	
	private final List<PolicyProbability> getInitialPolicyDistribution(List<KitchenSubdomain> subdomains) {
		List<PolicyProbability> distribution = new ArrayList<PolicyProbability>(subdomains.size());
		double uniformProbability = 1.0 / subdomains.size();
		
		for (KitchenSubdomain subdomain : subdomains) {
			PolicyProbability policyProbability = 
					PolicyProbability.newPolicyProbability(subdomain, uniformProbability);
			distribution.add(policyProbability);
		}
		
		return distribution;
	}

	@Override
	public AbstractGroundedAction getActionInState(State state) {
		List<PolicyProbability> policyDistribution = this.getPolicyDistribution(state, null);
		if (policyDistribution == null) {
			return null;
		}
		this.updateBeliefDistribution(policyDistribution);
		
		List<PolicyProbability> policyBeliefDistribution = Collections.unmodifiableList(this.policyBeliefDistribution);
		return (GroundedAction)this.getActionFromPolicyDistribution(policyBeliefDistribution, state);
		
	}
	
	@Override
	public AbstractGroundedAction getActionInStateWithScheduler(State state, List<String> agents, boolean finishRecipe, GroundedAction partnersAction) {
		if (!this.useScheduling) {
			return this.getActionInState(state);
		}
		
		List<PolicyProbability> nonZero = this.trimDistribution(this.policyBeliefDistribution);
		System.out.println("Current Distribution");
		for (PolicyProbability policyProb : nonZero) {
			System.out.println(policyProb.toString());
		}
		System.out.println("\n");
		/*
		for (PolicyProbability policy : nonZero) {
			System.out.println(policy.toString());
		}*/
		// For every policy, generate a list of actions for this state
		List<List<AbstractGroundedAction>> actionLists = this.generateActionLists(state, nonZero, finishRecipe);
		
		// Create workflows graphs from the actionLists 
		List<Workflow> workflows = AdaptiveAgent.generateWorkflows(state, actionLists);
		
		// Get the available actions for each workflow
		List<GroundedAction> availableActions = this.getAvailableActions(workflows, state);
		availableActions.add(new GroundedAction(null, new String[]{this.getAgentName()}));
		/*
		System.out.println("Possible actions to take:");
		for (GroundedAction action : availableActions) {
			System.out.println("\t" + action.toString());
		}*/
		ChooseHelpfulActionCallable callable = new ChooseHelpfulActionCallable(state, agents, nonZero, this.subdomains, this.timeGenerator, finishRecipe);
		List<Double> completionTimes = Parallel.ForEach(availableActions, callable);
		
		return (GroundedAction)this.findBestAction(availableActions, completionTimes);
	}
	
	

	private static Double expectedTimeOfTakingAction(State state,
			List<String> agents, List<PolicyProbability> policyDistribution, List<KitchenSubdomain> subdomains,
			GroundedAction action, ActionTimeGenerator timeGenerator, boolean finishRecipe) {
		
		State newState = state;
		if (action.action != null) {
			newState = action.executeIn(state);
		}
		
		// For every available action, generate a new list of actions, that have to be taken to accomodate the available action
		StringBuffer buffer = new StringBuffer();
		buffer.append(action.toString() + "\n");
		List<List<AbstractGroundedAction>> adjustedActionLists = 
				AdaptiveAgent.correctActionLists(newState, action, policyDistribution, subdomains, buffer, finishRecipe);
		System.out.println(buffer.toString());
		
		// For each adjusted action list, create the associated adjusted workflow
		List<Workflow> adjustedWorkflows = AdaptiveAgent.generateWorkflows(state, adjustedActionLists);
		
		Map<String, Double> startingDelays = new HashMap<String, Double>();
		if (action.action == null) {
			startingDelays.put(action.params[0], 10.0);
		}
		// For each workflow, create the assignments and compute the expected time of finishing that assignment 
		List<Double> expectedCompletionTimes = AdaptiveAgent.assignAllWorkflowsAndGetCompletionTimes(state, agents, adjustedWorkflows, timeGenerator, startingDelays);
		
		// Weight the completion time by the belief in the policy
		return AdaptiveAgent.getWeightedCompletionTimes(expectedCompletionTimes, policyDistribution);
	}
	
	protected List<PolicyProbability> trimDistribution(List<PolicyProbability> distribution) {
		List<PolicyProbability> trimmed = new ArrayList<PolicyProbability>(distribution.size());
		for (PolicyProbability policy : distribution) {
			if (policy.getProbability() > 0.0) {
				trimmed.add(policy);
			}
		}
		return trimmed;
	}
	protected List<List<AbstractGroundedAction>> generateActionLists(State state, List<PolicyProbability> policies, boolean finishRecipe) {
		List<List<AbstractGroundedAction>> actionLists = new ArrayList<List<AbstractGroundedAction>>();
		List<KitchenSubdomain> subdomains = new ArrayList<KitchenSubdomain>(this.subdomains);
		for (PolicyProbability policyProb : policies) {
			//System.out.println(policyProb.getPolicyDomain().toString());
			if (policyProb.getProbability() > 0.0) {
				KitchenSubdomain policy = policyProb.getPolicyDomain();
				List<GroundedAction> groundedActions = new ArrayList<GroundedAction>();
				List<KitchenSubdomain> remainingSubgoals = AdaptiveAgent.getRemainingSubgoals(policy, subdomains, state);
				State result = AgentHelper.generateActionSequence(policy, remainingSubgoals, state, rewardFunction, groundedActions, finishRecipe);
				List<AbstractGroundedAction> abstractActions = new ArrayList<AbstractGroundedAction>(groundedActions.size());
				TerminalFunction tf = policy.getTerminalFunction();
				if (tf.isTerminal(result)) {
					for (GroundedAction ga : groundedActions) {
						//System.out.println("\t" + ga.toString());
						abstractActions.add((AbstractGroundedAction)ga);
					}
				} else {
					//System.out.println("Policy didn't generate a good plan");
				}
				actionLists.add(abstractActions);
			} else {
				actionLists.add(new ArrayList<AbstractGroundedAction>());
			}
			
			
		}
		//System.out.println("");
		return actionLists;
	}
	
	protected static List<KitchenSubdomain> getRemainingSubgoals(KitchenSubdomain policyDomain, List<KitchenSubdomain> allSubdomains, State state) {
		BakingSubgoal current = policyDomain.getSubgoal();
		Recipe currentRecipe = policyDomain.getRecipe();
		List<BakingSubgoal> subgoals = currentRecipe.getSubgoals();
		Set<BakingSubgoal> toRemove = new HashSet<BakingSubgoal>(subgoals.size() * 2);
		toRemove.addAll(current.getPreconditions());
		for (BakingSubgoal subgoal : subgoals) {
			if (subgoal.goalCompleted(state)) {
				toRemove.add(subgoal);
			}
		}
		Set<BakingSubgoal> queue = new HashSet<BakingSubgoal>(toRemove);
		while (!queue.isEmpty()) {
			Iterator<BakingSubgoal> iterator = queue.iterator();
			BakingSubgoal subgoal = iterator.next();
			iterator.remove();
			for (BakingSubgoal condition : subgoal.getPreconditions()) {
				if (toRemove.add(condition)) {
					queue.add(condition);
				}
			}
		}
		
		
		List<KitchenSubdomain> remaining = new ArrayList<KitchenSubdomain>(allSubdomains.size());
		for (KitchenSubdomain subdomain : allSubdomains) {
			if (subdomain.getRecipe().equals(currentRecipe) && !toRemove.contains(subdomain.getSubgoal())) {
				remaining.add(subdomain);
			}
		}
		
		return remaining;
		
		
		
	}
	
	protected static List<Workflow> generateWorkflows(State state, List<List<AbstractGroundedAction>> actionLists) {
		List<Workflow> workflows = new ArrayList<Workflow>();
		for (List<AbstractGroundedAction> list : actionLists) {
			Workflow workflow = Workflow.buildWorkflow(state, list);
			workflows.add(workflow);
		}
		
		return workflows;
	}
	
	protected static List<Double> assignAllWorkflowsAndGetCompletionTimes(State state, List<String> agents, List<Workflow> workflows, ActionTimeGenerator timeGenerator, Map<String, Double> startingDelays) {
		Scheduler exhaustive = new ExhaustiveStarScheduler(false);
		List<Double> completionTimes = new ArrayList<Double>();
		Assignments assignments = new Assignments(timeGenerator, agents, state, false, false);
		for (Map.Entry<String, Double> entry : startingDelays.entrySet()) {
			assignments.waitAgentUntil(entry.getKey(), entry.getValue());
		}
		for (Workflow workflow : workflows) {
			Assignments completed = exhaustive.finishSchedule(workflow, assignments.copy(), timeGenerator);
			double time = (completed == null) ? Double.MAX_VALUE : completed.time();
			completionTimes.add(time);
		}
		return completionTimes;
	}
	
	protected static List<Double> generateExpectedCompletionTimes(List<List<Assignment>> allAssignments, Map<String, Double> startingDelays) {
		List<Double> expectedCompletionTimes = new ArrayList<Double>(allAssignments.size());
		for (List<Assignment> assignments : allAssignments) {
			Double longestTime = 0.0;
			
			for (Assignment workflow : assignments) {
				longestTime = Math.max(longestTime, workflow.time());
			}
			expectedCompletionTimes.add(longestTime);
		}
		return expectedCompletionTimes;
	}
	
	protected static Double getWeightedCompletionTimes(List<Double> expectedTimes, List<PolicyProbability> policyDistribution) {
		double weightedTime = 0.0;
		for (int i = 0; i < expectedTimes.size(); i++) {
			double time = expectedTimes.get(i);
			PolicyProbability policy = policyDistribution.get(i);
			weightedTime += time * policy.getProbability();
		}
		return weightedTime;
	}
	
	protected List<List<Double>> getBestCompletionTimes(List<List<List<Double>>> allCompletionTimes) {
		List<List<Double>> allBestTimes = new ArrayList<List<Double>>(allCompletionTimes.size());
		for (List<List<Double>> completionTimes : allCompletionTimes) {
			List<Double> bestTimes = new ArrayList<Double>(completionTimes.size());
			for (List<Double> times : completionTimes) {
				Double bestTime = Double.MAX_VALUE;
				for (Double time : times) {
					bestTime = Math.min(bestTime, time);
				}
				bestTimes.add(bestTime);
			}
			allBestTimes.add(bestTimes);
		}
		return allBestTimes;
	
	}
	
	protected List<GroundedAction> getAvailableActions(List<Workflow> workflows, State state) {
		
		Set<GroundedAction> availableActions = new HashSet<GroundedAction>();
		for (Workflow workflow : workflows) {
			for (Workflow.Node node : workflow.getReadyNodes()) {
				//System.out.println("\t" + node.toString() + ": " + node.getAction().toString());
				GroundedAction action = node.getAction();
				GroundedAction copy = (GroundedAction)action.copy();
				copy.params = action.params.clone();
				copy.params[0] = this.getAgentName();
				//System.out.println("\t\t" + copy.toString());
				if (copy.action.applicableInState(state, copy.params)) {
					availableActions.add(copy);
				}
			}
			//System.out.println("");
		}
		
		
		List<GroundedAction> possibleActions = new ArrayList<GroundedAction>(availableActions);
		return possibleActions;
	}
	
	protected List<State> executeActions(State state, Collection<GroundedAction> actions) {
		List<State> states = new ArrayList<State>(actions.size());
		for (GroundedAction action : actions) {
			states.add(action.executeIn(state));
		}
		return states;
	}
	
	// Generates States x policies 2D array of Action sequences
	protected static List<List<AbstractGroundedAction>> correctActionLists(State state, GroundedAction action, 
			List<PolicyProbability> policies, List<KitchenSubdomain> subdomains, StringBuffer buffer, boolean finishRecipe) {
		
		List<GroundedAction> actions = new ArrayList<GroundedAction>();
		
		List<List<AbstractGroundedAction>> actionLists = new ArrayList<List<AbstractGroundedAction>>(policies.size());
		for (PolicyProbability policyProb : policies) {
			KitchenSubdomain policy = policyProb.getPolicyDomain();
			
			actions.clear();
			if (action.action != null) {
				actions.add(action);
			}
			
			List<KitchenSubdomain> remainingSubgoals = AdaptiveAgent.getRemainingSubgoals(policy, subdomains, state);
			AgentHelper.generateActionSequence(remainingSubgoals, state, rewardFunction, actions, finishRecipe);
			buffer.append("\t" + policy.toString() + "\n");
			buffer.append("\t\t" + actions.toString() + "\n");
			List<AbstractGroundedAction> abstractActions = new ArrayList<AbstractGroundedAction>(actions.size());
			for (GroundedAction ga : actions) abstractActions.add((AbstractGroundedAction)ga);
			
			actionLists.add(abstractActions);
		}
		
		return actionLists;
		
	}
	
	
	protected AbstractGroundedAction findBestAction(List<GroundedAction> availableNodes, List<Double> expectedCompletionTimes) {
		if (availableNodes.size() != expectedCompletionTimes.size()) {
			return null;
		}
		double bestTime = Double.MAX_VALUE;
		AbstractGroundedAction bestAction = null;
		for (int i = 0; i < availableNodes.size(); i++) {
			double time = expectedCompletionTimes.get(i);
			if (time < bestTime) {
				bestTime = time;
				bestAction = availableNodes.get(i);
			}
			System.out.println(availableNodes.get(i).toString() + ": " + time);
		}

		return bestAction;
	}
	
	protected abstract List<PolicyProbability> getPolicyDistribution(State currentState, GroundedAction agentsAction);
	protected abstract AbstractGroundedAction getActionFromPolicyDistribution(List<PolicyProbability> policyDistribution, State state);
	protected abstract void init();
	@Override
	public void addObservation(State state, GroundedAction agentsAction) {
		State previousState = this.stateHistory.get(this.stateHistory.size()-1);
		if (agentsAction == null || !agentsAction.executeIn(previousState).equals(state)) {
			this.stateHistory.add(state);
		}
		
		
		System.out.println("Previous Distribution");
		for (PolicyProbability policyProb : this.policyBeliefDistribution) {
			System.out.println(policyProb.toString());
		}
		System.out.println("\n");
		
		List<PolicyProbability> policyDistribution = this.getPolicyDistribution(state, agentsAction);
		if (policyDistribution != null) {
			System.out.println("Update");
			for (PolicyProbability policyProb : policyDistribution) {
				System.out.println(policyProb.toString());
			}
			System.out.println("\n");
			
			this.updateBeliefDistribution(policyDistribution);
		}
		
	}
	
	protected void updateBeliefDistribution(List<PolicyProbability> updatePolicyDistribution) {
		
		double sumProbability = 0.0;
		double previousSumProbability = 0.0;
		double updateSumProbability = 0.0;
		for (int i = 0; i < this.policyBeliefDistribution.size(); i++) {
			PolicyProbability priorBelief = this.policyBeliefDistribution.get(i);
			double beliefProbability = priorBelief.getProbability();
			previousSumProbability += beliefProbability;
			
			PolicyProbability update = updatePolicyDistribution.get(i);
			if (!update.getPolicyDomain().equals(priorBelief.getPolicyDomain())) {
				System.err.println("This isn't doing what you thought");
			}
			double updateProbability = update.getProbability();
			updateSumProbability += updateProbability;
			
			double newProbability = beliefProbability * updateProbability;
			sumProbability += newProbability;
			PolicyProbability updatedBelief = PolicyProbability.updatePolicyProbability(priorBelief, newProbability);
			this.policyBeliefDistribution.set(i, updatedBelief);
			
		}
		
		if (previousSumProbability == 0.0) {
			//System.err.println("All previous probabilities are 0.0");
		}
		
		if (updateSumProbability == 0.0) {
			//System.err.println("All update probabilities are 0.0");
		}
		
		
		for (int i = 0; i < this.policyBeliefDistribution.size(); i++) {
			PolicyProbability belief = this.policyBeliefDistribution.get(i);
			double beliefProbability = belief.getProbability();
			
			double normalizedProbability = (sumProbability == 0.0) ? 
					1.0 / this.policyBeliefDistribution.size() : 
						beliefProbability / sumProbability;
			PolicyProbability normalizedBelief = PolicyProbability.updatePolicyProbability(belief, normalizedProbability);
			this.policyBeliefDistribution.set(i, normalizedBelief);
			
		}
	}
	
	protected abstract double getTransitionProbability(Policy from, Policy to);
	
	private static class ChooseHelpfulActionCallable extends ForEachCallable<GroundedAction, Double> {
		private State state;
		private List<String> agents;
		private List<PolicyProbability> distribution;
		private List<KitchenSubdomain> subdomains;
		private GroundedAction item;
		private ActionTimeGenerator timeGenerator;
		private boolean finishRecipe;
		
		
		public ChooseHelpfulActionCallable(State state,
			List<String> agents, List<PolicyProbability> policyDistribution, List<KitchenSubdomain> subdomains, ActionTimeGenerator timeGenerator, boolean finishRecipe) {
			this.state = state;
			this.agents = agents;
			this.distribution = policyDistribution;
			this.timeGenerator = timeGenerator;
			this.finishRecipe = finishRecipe;
			this.subdomains = new ArrayList<KitchenSubdomain>(subdomains);
		}
		public ChooseHelpfulActionCallable(ChooseHelpfulActionCallable base, GroundedAction item) {
			this.state = base.state;
			this.agents = base.agents;
			this.distribution = base.distribution;
			this.item = item;
			this.timeGenerator = base.timeGenerator;
			this.finishRecipe = base.finishRecipe;
			this.subdomains = base.subdomains;
		}
		@Override
		public Double call() throws Exception {
			return AdaptiveAgent.expectedTimeOfTakingAction(state, agents, distribution, this.subdomains, item, timeGenerator, finishRecipe);
		}
		@Override
		public ForEachCallable<GroundedAction, Double> init(
				GroundedAction current) {
			return new ChooseHelpfulActionCallable(this, current);
		}
	}
	
}
