package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Prediction.PolicyProbability;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveScheduler;
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
		this.updateBeliefDistribution(policyDistribution);
		
		// For every policy, generate a list of actions for this state
		List<List<AbstractGroundedAction>> actionLists = this.generateActionLists(state, policyDistribution);
		
		// Create workflows graphs from the actionLists 
		List<Workflow> workflows = this.generateWorkflows(state, actionLists);
		
		// Get the available actions for each workflow
		List<Workflow.Node> availableActions = this.getAvailableActions(workflows);
		
		// For every availabel action, generate a new list of actions, that have to be taken to accomodate the available action
		List<List<List<AbstractGroundedAction>>> adjustedActionLists = 
				this.correctActionLists(state, availableActions, policyDistribution);
		
		// For each adjusted action list, create the associated adjusted workflow
		List<List<Workflow>> adjustedWorkflows = this.generateAllWorkflows(state, adjustedActionLists);
		
		// For each workflow, create the optimal assignments
		List<List<List<AssignedWorkflow>>> assignedWorkflows = this.assignAllWorkflows(state, agents, adjustedWorkflows);
		
		// For each assignment, compute how long each assignment would take
		List<List<Double>> expectedCompletionTimes = this.generateExpectedCompletionTimes(assignedWorkflows);
		
		// Weight the completion time by the belief in the policy
		List<Double> bestCompletionTimes = this.getWeightedCompletionTimes(expectedCompletionTimes, policyDistribution);
		
		return this.findBestAction(availableActions, bestCompletionTimes);
	}
	
	protected List<List<AbstractGroundedAction>> generateActionLists(State state, List<PolicyProbability> policies) {
		List<List<AbstractGroundedAction>> actionLists = new ArrayList<List<AbstractGroundedAction>>();
		
		for (PolicyProbability policyProb : policies) {
			List<GroundedAction> groundedActions = new ArrayList<GroundedAction>();
			AgentHelper.generateActionSequence(policyProb.getPolicyDomain(), state, groundedActions);
			List<AbstractGroundedAction> abstractActions = new ArrayList<AbstractGroundedAction>();
			for (GroundedAction ga : groundedActions) abstractActions.add((AbstractGroundedAction)ga);
			actionLists.add(abstractActions);
		}
		return actionLists;
	}
	
	protected List<Workflow> generateWorkflows(State state, List<List<AbstractGroundedAction>> actionLists) {
		List<Workflow> workflows = new ArrayList<Workflow>();
		for (List<AbstractGroundedAction> list : actionLists) {
			Workflow workflow = Workflow.buildWorkflow(state, list);
			workflows.add(workflow);
		}
		
		return workflows;
	}
	
	protected List<List<Workflow>> generateAllWorkflows(State state, List<List<List<AbstractGroundedAction>>> allActionLists) {
		List<List<Workflow>> allWorkflows = new ArrayList<List<Workflow>>(allActionLists.size());
		for (List<List<AbstractGroundedAction>> actionLists : allActionLists) {
			allWorkflows.add(this.generateWorkflows(state, actionLists));
		}
		return allWorkflows;
	}
	
	protected List<List<List<AssignedWorkflow>>> assignAllWorkflows(State state, List<String> agents, List<List<Workflow>> allWorkflows) {
		List<List<List<AssignedWorkflow>>> allAssignments = new ArrayList<List<List<AssignedWorkflow>>>(allWorkflows.size());
		Scheduler exhaustive = new ExhaustiveScheduler(5);
		for (List<Workflow> workflows : allWorkflows) {
			List<List<AssignedWorkflow>> assignments = new ArrayList<List<AssignedWorkflow>>(workflows.size());
			for (Workflow workflow : workflows) {
				assignments.add(exhaustive.schedule(workflow, agents, this.timeGenerator));
			}
			allAssignments.add(assignments);
		}
		
		return allAssignments;
	}
	
	protected List<List<Double>> generateExpectedCompletionTimes(List<List<List<AssignedWorkflow>>> allAssignedWorkflows) {
		List<List<Double>> allExpectedCompletionTimes = new ArrayList<List<Double>>(allAssignedWorkflows.size());
		for (List<List<AssignedWorkflow>> allAssignments : allAssignedWorkflows) {
			List<Double> expectedCompletionTimes = new ArrayList<Double>(allAssignments.size());
			for (List<AssignedWorkflow> assignments : allAssignments) {
				Double longestTime = 0.0;
				for (AssignedWorkflow workflow : assignments) {
					longestTime = Math.max(longestTime, workflow.time());
				}
				expectedCompletionTimes.add(longestTime);
			}
			allExpectedCompletionTimes.add(expectedCompletionTimes);
		}
		return allExpectedCompletionTimes;
	}
	
	protected List<Double> getWeightedCompletionTimes(List<List<Double>> expectedCompletionTimes, List<PolicyProbability> policyDistribution) {
		List<Double> weightedCompletionTimes = new ArrayList<Double>(expectedCompletionTimes.size());
		
		for (List<Double> expectedTimes : expectedCompletionTimes) {
			double weightedTime = 0.0;
			for (int i = 0; i < expectedTimes.size(); i++) {
				double time = expectedTimes.get(i);
				PolicyProbability policy = policyDistribution.get(i);
				weightedTime += time * policy.getProbability();
			}
			weightedCompletionTimes.add(weightedTime);
		}
		return weightedCompletionTimes;
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
	
	protected List<Workflow.Node> getAvailableActions(List<Workflow> workflows) {
		
		List<Workflow.Node> availableActions = new ArrayList<Workflow.Node>();
		for (Workflow workflow : workflows) {
			availableActions.addAll(workflow.getReadyNodes());
		}
		
		return availableActions;
	}
	
	protected List<List<List<AbstractGroundedAction>>> correctActionLists(State state,
			List<Workflow.Node> availableActions, List<PolicyProbability> policies) {
		
		List<List<List<AbstractGroundedAction>>> correctedActionLists = 
				new ArrayList<List<List<AbstractGroundedAction>>>(availableActions.size());
	
		for (Workflow.Node node : availableActions) {
			AbstractGroundedAction action = node.getAction();
			State newState = action.executeIn(state);
			List<List<AbstractGroundedAction>> actionLists = new ArrayList<List<AbstractGroundedAction>>(policies.size());
			
			for (PolicyProbability policyProb : policies) {
				List<GroundedAction> actions = new ArrayList<GroundedAction>();
				AgentHelper.generateActionSequence(policyProb.getPolicyDomain(), newState, actions);
				List<AbstractGroundedAction> abstractActions = new ArrayList<AbstractGroundedAction>(actions.size());
				
				for (GroundedAction ga : actions) abstractActions.add((AbstractGroundedAction)ga);
				actionLists.add(abstractActions);	
			}
			correctedActionLists.add(actionLists);
		}
		
		return correctedActionLists;
		
	}
	
	
	protected AbstractGroundedAction findBestAction(List<Workflow.Node> availableNodes, List<Double> expectedCompletionTimes) {
		double bestTime = Double.MAX_VALUE;
		AbstractGroundedAction bestAction = null;
		for (int i = 0; i < availableNodes.size(); i++) {
			double time = expectedCompletionTimes.get(i);
			if (time < bestTime) {
				bestTime = time;
				bestAction = availableNodes.get(i).getAction();
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
	
}
