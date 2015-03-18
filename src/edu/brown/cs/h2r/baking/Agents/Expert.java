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
import edu.brown.cs.h2r.baking.Scheduling.Assignments;
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
	
	public Expert(Domain domain, String name, boolean isRobot, ActionTimeGenerator timeGenerator, List<Recipe> recipes)  {
		super(domain, name, isRobot, timeGenerator, recipes);
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
	public AbstractGroundedAction getActionInStateWithScheduler(State state, List<String> agents, boolean finishRecipe, GroundedAction partnersAction) {
		State newState = state;
		if (partnersAction != null) {
			newState = partnersAction.executeIn(state);
			if (newState.equals(state)) {
				partnersAction = null;
			}
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
			aga.add(0, partnersAction);
		}
		Workflow workflow = Workflow.buildWorkflow(state, aga);
		
		if (this.isCooperative) {
			
			Assignments assignments = scheduleActions(state, agents, partnersAction, workflow);
			System.out.println(this.getAgentName() + " scheduled actions");
			for (Assignment assignment : assignments) {
				System.out.println("-" + assignment.getId());
				List<Workflow.Node> nodes = assignment.nodes();
				List<Double> times = assignment.times();
				List<Double> completionTimes = assignment.completionTimes();
				for (int i = 0; i < nodes.size(); i++) {
					Workflow.Node node = nodes.get(i);
					double time = times.get(i);
					double completionTime = completionTimes.get(i);
					String line = "\t" + ((node == null) ? "wait": node.getAction(assignment.getId()).toString());
					line = line + ", " + (completionTime - time) + ", " + completionTime;
					System.out.println(line);
				}
			}
			
			GroundedAction action = assignments.getFirstAction(this.getAgentName());
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

	private Assignments scheduleActions(State state, List<String> agents,
			GroundedAction partnersAction, Workflow workflow) {
		
		Scheduler exhaustive = new ExhaustiveStarScheduler(true);
		
		if (partnersAction == null) {
			return exhaustive.schedule(workflow, agents, this.timeGenerator);
		}
		
		Assignments assignments = new Assignments(this.timeGenerator, agents, state, false, false);
		
		if (partnersAction != null) {
			assignments.add(workflow.get(0), partnersAction.params[0]);
		} 
		
		return exhaustive.finishSchedule(workflow, assignments, this.timeGenerator);

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
