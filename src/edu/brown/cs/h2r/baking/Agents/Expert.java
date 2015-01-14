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
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveStarScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;

public class Expert extends Human implements Agent {
	private String name;
	public Expert(Domain domain, String name, ActionTimeGenerator timeGenerator) {
		super(domain, timeGenerator);
		this.name = name;
	}
	
	@Override
	public void addObservation(State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAgentName() {
		return this.name;
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
}
