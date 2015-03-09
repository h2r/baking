package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
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
import edu.brown.cs.h2r.baking.actions.ResetAction;

public class Expert extends Human{
	private boolean isCooperative;
	public Expert(Domain domain, String name, ActionTimeGenerator timeGenerator, List<Recipe> recipes)  {
		super(domain, name, timeGenerator, recipes);
		this.isCooperative = true;
	}
	
	protected Expert(Domain domain, Map<String, Object> map, ActionTimeGenerator timeGenerator, State state, List<Recipe> recipes) {
		super(domain, map, timeGenerator, state, recipes);
		this.isCooperative = (Boolean)map.get("isCooperative");
	}
	
	@Override
	protected Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();
		map.put("isCooperative", this.isCooperative);
		return map;
	}
	
	@Override
	public void addObservation(State state) {
	}
	
	public void setCooperative(boolean isCooperative) {
		this.isCooperative = isCooperative;
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
	
	@Override
	public AbstractGroundedAction getActionWithScheduler(State state, List<String> agents, boolean finishRecipe) {
		return this.getActionWithScheduler(state, agents, finishRecipe, null);
	}
	
	public AbstractGroundedAction getActionWithScheduler(State state, List<String> agents, boolean finishRecipe, GroundedAction partnersAction) {
		State newState = state;
		if (partnersAction != null) {
			//newState = partnersAction.executeIn(state);
		}
		if (this.isSuccess(newState)) {
			return null;
		}
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(newState);
		} else if (this.currentSubgoal.getSubgoal().goalCompleted(newState)) {
			this.getKitchenSubdomains().remove(this.currentSubgoal);
			this.chooseNewSubgoal(newState);
		}
		if (this.currentSubgoal == null) {
			return null;
		}
		
		List<KitchenSubdomain> subdomains = new ArrayList<KitchenSubdomain>(this.getKitchenSubdomains());
		List<KitchenSubdomain> remaining = Expert.getRemainingSubgoals(this.currentSubgoal, subdomains, newState);
		List<GroundedAction> actions = new ArrayList<GroundedAction>();
		AgentHelper.generateActionSequence(this.currentSubgoal, remaining, newState, rewardFunction, actions, finishRecipe);
		
		if (actions.size() > 0 && actions.get(0).action instanceof ResetAction) {
			GroundedAction reset = actions.get(0);
			reset.params[0] = this.getAgentName();
			this.setRecipe(this.currentRecipe);
			return reset;
		}
		
		List<AbstractGroundedAction> aga = new ArrayList<AbstractGroundedAction>(actions);
		if (partnersAction != null) {
			boolean foundMatch = false;
			for (GroundedAction ga : actions) {
				if (ga.action == partnersAction.action) {
					foundMatch = true;
					for (int i = 1 ; i < partnersAction.params.length; i++) {
						if (!ga.params[i].equals(partnersAction.params[i])) {
							foundMatch = false;
						}
					}
				}
			}
			if (!foundMatch) {
				aga.add(0, partnersAction);
			}
		}
		Workflow workflow = Workflow.buildWorkflow(state, aga);
		
		if (this.isCooperative) {
			List<Assignment> assignments = scheduleActions(state, agents, partnersAction, workflow);
			System.out.println(this.getAgentName() + " scheduled actions");
			for (Assignment assignment : assignments) {
				System.out.println("-" + assignment.getId());
				List<Workflow.Node> nodes = assignment.nodes();
				for (Workflow.Node node : nodes) {
					System.out.println("\t" + node.getAction(assignment.getId()).toString());
				}
			}
			
			BufferedAssignments buffered = new BufferedAssignments(assignments, false);
			GroundedAction action = buffered.getFirstAction(this.getAgentName());
			if (action == null) {
				return null;
			}
			return action;
		} else {
			List<Workflow.Node> available = workflow.getReadyNodes();
			GroundedAction bestAction = null;
			double bestTime = Double.MAX_VALUE;
			for (Workflow.Node node : available) {
				GroundedAction ga = node.getAction(this.getAgentName());
				double time = this.timeGenerator.get(ga, true);
				if (!this.matchingActions(ga, partnersAction) && time < bestTime) {
					bestAction = ga;
					bestTime = time;
				}
			}
			return bestAction;
		}
	}

	private List<Assignment> scheduleActions(State state, List<String> agents,
			GroundedAction partnersAction, Workflow workflow) {
		
		Scheduler exhaustive = new ExhaustiveStarScheduler(true);
		
		if (partnersAction == null) {
			return exhaustive.schedule(workflow, agents, this.timeGenerator);
		}
		
		List<Assignment> assignments = new ArrayList<Assignment>();
		Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(workflow.size() * 2); 
		
		Assignment humansAssignment = new Assignment(this.getAgentName(), this.timeGenerator, true);
		assignments.add(humansAssignment);
		
		Assignment partnerAssignment = new Assignment(partnersAction.params[0], this.timeGenerator, true);
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
		
		BufferedAssignments bufferedAssignments = new BufferedAssignments(assignments, false);
		assignments = exhaustive.finishSchedule(workflow, this.timeGenerator, assignments, bufferedAssignments, visitedNodes);
		return assignments;
	}
	
	private boolean matchingActions(GroundedAction lhs, GroundedAction rhs) {
		if (lhs == null || rhs == null) {
			return false;
		}
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
