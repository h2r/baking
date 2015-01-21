package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Assignment;
import edu.brown.cs.h2r.baking.Scheduling.BufferedAssignments;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveStarScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class Expert extends Human implements Agent {
	public Expert(Domain domain, String name, ActionTimeGenerator timeGenerator) {
		super(domain, name, timeGenerator);
	}
	
	@Override
	public void addObservation(State state) {
		// TODO Auto-generated method stub

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
	
	public AbstractGroundedAction getActionWithScheduler(State state, List<String> agents, boolean finishRecipe) {
		if (this.isSuccess(state)) {
			return null;
		}
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(state);
		} else if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
			this.getKitchenSubdomains().remove(this.currentSubgoal);
			this.chooseNewSubgoal(state);
		}
		
		List<KitchenSubdomain> subdomains = new ArrayList<KitchenSubdomain>(this.getKitchenSubdomains());
		List<KitchenSubdomain> remaining = Expert.getRemainingSubgoals(this.currentSubgoal, subdomains, state);
		List<GroundedAction> actions = new ArrayList<GroundedAction>();
		AgentHelper.generateActionSequence(remaining, state, rewardFunction, actions, finishRecipe);
		
		List<AbstractGroundedAction> aga = new ArrayList<AbstractGroundedAction>(actions);
		
		Workflow workflow = Workflow.buildWorkflow(state, aga);
		
		Scheduler exhaustive = new ExhaustiveStarScheduler();
		List<Assignment> assignments = exhaustive.schedule(workflow, agents, timeGenerator);
		Integer location = agents.indexOf(this.getAgentName());
		Assignment assignment = assignments.get(location);
		Workflow.Node first = assignment.first();
		if (first == null) {
			return null;
		}
		return first.getAction();
		/*if (this.isSuccess(state)) {
			return null;
		}
		
		List<AbstractGroundedAction> actions = this.generateActionList(state);
		Workflow workflow = Workflow.buildWorkflow(state, actions);
		List<Assignment> assignments = this.scheduler.schedule(workflow, agents, this.timeGenerator);
		for (Assignment assignment : assignments) {
			if (assignment.getId().equals(this.getAgentName())) {
				for (ActionTime actionTime : assignment) {
					return actionTime.getNode().getAction();
				}
			}
		}
		
		return null;*/
	}
	
	public AbstractGroundedAction getActionWithScheduler(State state, List<String> agents, boolean finishRecipe, GroundedAction partnersAction) {
		if (this.isSuccess(state)) {
			return null;
		}
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(state);
		} else if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
			this.getKitchenSubdomains().remove(this.currentSubgoal);
			this.chooseNewSubgoal(state);
		}
		if (this.currentSubgoal == null) {
			return null;
		}
		
		List<KitchenSubdomain> subdomains = new ArrayList<KitchenSubdomain>(this.getKitchenSubdomains());
		List<KitchenSubdomain> remaining = Expert.getRemainingSubgoals(this.currentSubgoal, subdomains, state);
		List<GroundedAction> actions = new ArrayList<GroundedAction>();
		AgentHelper.generateActionSequence(this.currentSubgoal, remaining, state, rewardFunction, actions, finishRecipe);
		
		List<AbstractGroundedAction> aga = new ArrayList<AbstractGroundedAction>(actions);
		
		List<Assignment> assignments = scheduleActions(state, agents, partnersAction, aga);
		System.out.println(this.getAgentName() + " scheduled actions");
		for (Assignment assignment : assignments) {
			System.out.println("-" + assignment.getId());
			List<Workflow.Node> nodes = assignment.nodes();
			for (Workflow.Node node : nodes) {
				System.out.println("\t" + node.getAction().toString());
			}
		}
		for (Assignment assignment : assignments) {
			if (assignment.getId().equals(this.getAgentName()) && assignment.first() != null) {
				Workflow.Node first = assignment.first();
				GroundedAction action = first.getAction();
				action.params[0] = this.getAgentName();
				if (partnersAction != null && action.params[0].equals(partnersAction.params[0])) {
					throw new RuntimeException("Action agents shouldn't be the same");
				}
				return action;
			}
		}
		return null;
	}

	private List<Assignment> scheduleActions(State state, List<String> agents,
			GroundedAction partnersAction, List<AbstractGroundedAction> aga) {
		
		Workflow workflow = Workflow.buildWorkflow(state, aga);
		Scheduler exhaustive = new ExhaustiveStarScheduler();
		
		if (partnersAction == null) {
			return exhaustive.schedule(workflow, agents, this.timeGenerator);
		}
		
		List<Assignment> assignments = new ArrayList<Assignment>();
		Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(workflow.size() * 2); 
		
		Assignment humansAssignment = new Assignment(this.getAgentName(), this.timeGenerator);
		assignments.add(humansAssignment);
		
		Assignment partnerAssignment = new Assignment(partnersAction.params[0], this.timeGenerator);
		assignments.add(partnerAssignment);
		
		boolean matchedAction = false;
		Workflow.Node partnersNode = null;
		for (Workflow.Node node : workflow) {
			if (this.matchingActions(node.getAction(), partnersAction)) {
				matchedAction = true;
				partnersNode = node;
				break;
			}
		}
		
		
		if (!matchedAction) {
			State nextState = state;
			Workflow.Node newNode = new Workflow.Node(workflow.size(), partnersAction);
			
			Workflow.Node node = null;
			for (int i = 0; i < workflow.size(); i++) {
				if (partnersAction.action.applicableInState(nextState, partnersAction.params)) {
					if (node != null) {
						newNode.addParent(node);
					}
					workflow.insert(i, newNode);
					partnersNode = newNode;
					break;
				}
				
				node  = workflow.get(i);
				nextState = node.getAction().executeIn(nextState);
				humansAssignment.add(node);
				visitedNodes.add(node);
			}
		}
		if (partnersNode != null) {
			partnerAssignment.add(partnersNode);
			visitedNodes.add(partnersNode);
			
		}
		
		BufferedAssignments bufferedAssignments = new BufferedAssignments(assignments);
		assignments = exhaustive.finishSchedule(workflow, this.timeGenerator, assignments, bufferedAssignments, visitedNodes);
		return assignments;
	}
	
	private boolean matchingActions(GroundedAction lhs, GroundedAction rhs) {
		if (!lhs.action.equals(rhs.action)){ 
			return false;
		}
		for (int i = 1; i < lhs.params.length; i++) {
			if (!lhs.params[i].equals(rhs.params[i])) {
				return false;
			}
		}
		return true;
	}
}
