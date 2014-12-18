package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.parallel.Parallel;
import burlap.parallel.Parallel.ForEachCallable;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Prediction.PolicyProbability;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveStarScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.actions.ResetAction;

public abstract class AdaptiveAgent implements Agent {
	private final Domain domain;
	protected final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	protected final ActionTimeGenerator timeGenerator;
	protected final static RewardFunction rewardFunction = new RewardFunction() {
	
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			// TODO Auto-generated method stub
			return (a.action instanceof ResetAction) ? -2 : -1;
		}
		
	};
	protected final List<State> stateHistory;
	private final List<PolicyProbability> policyBeliefDistribution;
	protected final List<KitchenSubdomain> subdomains;
	
	public AdaptiveAgent(Domain domain, ActionTimeGenerator timeScheduler) {
		this.domain = domain;
		this.stateHistory = new ArrayList<State>();
		this.subdomains = new ArrayList<KitchenSubdomain>();
		this.policyBeliefDistribution = new ArrayList<PolicyProbability>();
		this.timeGenerator = timeScheduler;
	}
	
	@Override
	public ObjectInstance getAgentObject() {
		return AgentFactory.getNewHumanAgentObjectInstance(this.domain, this.getAgentName(), this.hashingFactory.getObjectHashFactory());
	}
	
	@Override
	public void setInitialState(State state) {
		this.stateHistory.clear();
		this.subdomains.clear();
		this.policyBeliefDistribution.clear();
		this.stateHistory.add(state);
		List<KitchenSubdomain> subdomains = AgentHelper.generateAllRTDPPolicies(domain, state, AgentHelper.recipes(domain),
				AdaptiveAgent.rewardFunction ,AdaptiveAgent.hashingFactory);
		this.subdomains.addAll(subdomains);
		this.policyBeliefDistribution.addAll(this.getInitialPolicyDistribution(subdomains));
		this.init();
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
	public AbstractGroundedAction getAction(State state) {
		List<PolicyProbability> policyDistribution = this.getPolicyDistribution(state);
		if (policyDistribution == null) {
			return null;
		}
		this.updateBeliefDistribution(policyDistribution);
		
		List<PolicyProbability> policyBeliefDistribution = Collections.unmodifiableList(this.policyBeliefDistribution);
		return this.getActionFromPolicyDistribution(policyBeliefDistribution, state);
		
	}
	
	@Override
	public AbstractGroundedAction getActionWithScheduler(State state, List<String> agents) {
		List<PolicyProbability> policyDistribution = this.getPolicyDistribution(state);
		
		if (policyDistribution == null) {
			return null;
		}
		for (PolicyProbability policy : policyDistribution) {
			//System.out.println(policy.toString());
		}
		this.updateBeliefDistribution(policyDistribution);
		List<PolicyProbability> nonZero = this.trimDistribution(policyDistribution);
		for (PolicyProbability policy : nonZero) {
			//System.out.println(policy.toString());
		}
		// For every policy, generate a list of actions for this state
		List<List<AbstractGroundedAction>> actionLists = this.generateActionLists(state, nonZero);
		
		// Create workflows graphs from the actionLists 
		List<Workflow> workflows = AdaptiveAgent.generateWorkflows(state, actionLists);
		
		// Get the available actions for each workflow
		List<GroundedAction> availableActions = this.getAvailableActions(workflows);
		
		ChooseHelpfulActionCallable callable = new ChooseHelpfulActionCallable(state, agents, policyDistribution, this.timeGenerator);
		List<Double> completionTimes = Parallel.ForEach(availableActions, callable);
		return this.findBestAction(availableActions, completionTimes);
	}
	
	

	private static Double expectedTimeOfTakingAction(State state,
			List<String> agents, List<PolicyProbability> policyDistribution,
			GroundedAction action, ActionTimeGenerator timeGenerator) {
		
		State newState = action.executeIn(state);
		
		// For every available action, generate a new list of actions, that have to be taken to accomodate the available action
		List<List<AbstractGroundedAction>> adjustedActionLists = 
				AdaptiveAgent.correctActionLists(newState, policyDistribution);
		
		// For each adjusted action list, create the associated adjusted workflow
		List<Workflow> adjustedWorkflows = AdaptiveAgent.generateWorkflows(state, adjustedActionLists);
		
		// For each workflow, create the optimal assignments
		List<List<AssignedWorkflow>> assignedWorkflows = AdaptiveAgent.assignAllWorkflows(state, agents, adjustedWorkflows, timeGenerator);
		
		// For each assignment, compute how long each assignment would take
		List<Double> expectedCompletionTimes = AdaptiveAgent.generateExpectedCompletionTimes(assignedWorkflows);
		
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
	protected List<List<AbstractGroundedAction>> generateActionLists(State state, List<PolicyProbability> policies) {
		List<List<AbstractGroundedAction>> actionLists = new ArrayList<List<AbstractGroundedAction>>();
		
		for (PolicyProbability policyProb : policies) {
			if (policyProb.getProbability() > 0.0) {
				List<GroundedAction> groundedActions = new ArrayList<GroundedAction>();
				AgentHelper.generateActionSequence(policyProb.getPolicyDomain(), state, groundedActions);
				List<AbstractGroundedAction> abstractActions = new ArrayList<AbstractGroundedAction>(groundedActions.size());
				for (GroundedAction ga : groundedActions) abstractActions.add((AbstractGroundedAction)ga);
				actionLists.add(abstractActions);
			} else {
				actionLists.add(new ArrayList<AbstractGroundedAction>());
			}
			
			
		}
		return actionLists;
	}
	
	protected static List<Workflow> generateWorkflows(State state, List<List<AbstractGroundedAction>> actionLists) {
		List<Workflow> workflows = new ArrayList<Workflow>();
		for (List<AbstractGroundedAction> list : actionLists) {
			Workflow workflow = Workflow.buildWorkflow(state, list);
			workflows.add(workflow);
		}
		
		return workflows;
	}
	
	protected static List<List<AssignedWorkflow>> assignAllWorkflows(State state, List<String> agents, List<Workflow> workflows, ActionTimeGenerator timeGenerator) {
		Scheduler exhaustive = new ExhaustiveStarScheduler();
		List<List<AssignedWorkflow>> assignments = new ArrayList<List<AssignedWorkflow>>(workflows.size());
		for (Workflow workflow : workflows) {
			assignments.add(exhaustive.schedule(workflow, agents, timeGenerator));
		}
		return assignments;
	}
	
	protected static List<Double> generateExpectedCompletionTimes(List<List<AssignedWorkflow>> allAssignments) {
		List<Double> expectedCompletionTimes = new ArrayList<Double>(allAssignments.size());
		for (List<AssignedWorkflow> assignments : allAssignments) {
			Double longestTime = 0.0;
			for (AssignedWorkflow workflow : assignments) {
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
	
	protected List<GroundedAction> getAvailableActions(List<Workflow> workflows) {
		
		Set<GroundedAction> availableActions = new HashSet<GroundedAction>();
		for (Workflow workflow : workflows) {
			for (Workflow.Node node : workflow.getReadyNodes()) {
				availableActions.add(node.getAction());
			}
		}
		
		return new ArrayList<GroundedAction>(availableActions);
	}
	
	protected List<State> executeActions(State state, Collection<GroundedAction> actions) {
		List<State> states = new ArrayList<State>(actions.size());
		for (GroundedAction action : actions) {
			states.add(action.executeIn(state));
		}
		return states;
	}
	
	// Generates States x policies 2D array of Action sequences
	protected static List<List<AbstractGroundedAction>> correctActionLists(State state, List<PolicyProbability> policies) {
		
		List<GroundedAction> actions = new ArrayList<GroundedAction>();
		
		List<List<AbstractGroundedAction>> actionLists = new ArrayList<List<AbstractGroundedAction>>(policies.size());
		
		for (PolicyProbability policyProb : policies) {
			KitchenSubdomain policy = policyProb.getPolicyDomain();
			
			actions.clear();
			AgentHelper.generateActionSequence(policy, state, actions);
			
			List<AbstractGroundedAction> abstractActions = new ArrayList<AbstractGroundedAction>(actions.size());
			for (GroundedAction ga : actions) abstractActions.add((AbstractGroundedAction)ga);
			
			actionLists.add(abstractActions);
		}
		
		return actionLists;
		
	}
	
	
	protected AbstractGroundedAction findBestAction(List<GroundedAction> availableNodes, List<Double> expectedCompletionTimes) {
		double bestTime = Double.MAX_VALUE;
		AbstractGroundedAction bestAction = null;
		for (int i = 0; i < availableNodes.size(); i++) {
			double time = expectedCompletionTimes.get(i);
			if (time < bestTime) {
				bestTime = time;
				bestAction = availableNodes.get(i);
			}
		}

		return bestAction;
	}
	
	protected abstract List<PolicyProbability> getPolicyDistribution(State currentState);
	protected abstract AbstractGroundedAction getActionFromPolicyDistribution(List<PolicyProbability> policyDistribution, State state);
	protected abstract void init();
	@Override
	public void addObservation(State state) {
		this.stateHistory.add(state);
		
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
			
			double normalizedProbability = (sumProbability == 0.0) ? 0.0 : beliefProbability / sumProbability;
			PolicyProbability normalizedBelief = PolicyProbability.updatePolicyProbability(belief, normalizedProbability);
			this.policyBeliefDistribution.set(i, normalizedBelief);
			
		}
	}
	
	protected abstract double getTransitionProbability(Policy from, Policy to);
	
	private static class ChooseHelpfulActionCallable extends ForEachCallable<GroundedAction, Double> {
		private State state;
		private List<String> agents;
		private List<PolicyProbability> distribution;
		private GroundedAction item;
		private ActionTimeGenerator timeGenerator;
		
		public ChooseHelpfulActionCallable(State state,
			List<String> agents, List<PolicyProbability> policyDistribution, ActionTimeGenerator timeGenerator) {
			this.state = state;
			this.agents = agents;
			this.distribution = policyDistribution;
			this.timeGenerator = timeGenerator;
		}
		public ChooseHelpfulActionCallable(ChooseHelpfulActionCallable base, GroundedAction item) {
			this.state = base.state;
			this.agents = base.agents;
			this.distribution = base.distribution;
			this.item = item;
			this.timeGenerator = base.timeGenerator;
		}
		@Override
		public Double call() throws Exception {
			return AdaptiveAgent.expectedTimeOfTakingAction(state, agents, distribution, item, timeGenerator);
		}
		@Override
		public ForEachCallable<GroundedAction, Double> init(
				GroundedAction current) {
			return new ChooseHelpfulActionCallable(this, current);
		}
	}
	
}
