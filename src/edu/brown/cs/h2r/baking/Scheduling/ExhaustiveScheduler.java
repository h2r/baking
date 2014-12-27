package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class ExhaustiveScheduler implements Scheduler {
	private final int maxDepth;
	
	public ExhaustiveScheduler() {
		this.maxDepth = -1;
	}
	
	public ExhaustiveScheduler(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	@Override
	public List<Assignment> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		Map<String, Assignment> assignedWorkflows = new HashMap<String, Assignment>();
		
		for (String agent : agents) {
			Assignment assignedWorkflow = new Assignment(agent);
			assignedWorkflows.put(agent, assignedWorkflow);
		}
		int previousSize = 0;
		while (previousSize != workflow.size()) {
			this.assignActions(workflow, actionTimeLookup, assignedWorkflows, new HashSet<Workflow.Node>(), this.maxDepth);
			
			previousSize = 0;
			for (Assignment assignedWorkflow : assignedWorkflows.values()) {
				previousSize += assignedWorkflow.size();
			}
		}
		
		return new ArrayList<Assignment>(assignedWorkflows.values());
	}
	
	public List<Assignment> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<Assignment> assignedWorkflows, BufferedAssignments bufferedWorkflows, Set<Workflow.Node> visitedNodes) {
		
		Map<String, Assignment> assignedWorkflowMap = new HashMap<String, Assignment>();
		for (Assignment assignedWorkflow : assignedWorkflows) {
			assignedWorkflowMap.put(assignedWorkflow.getId(), assignedWorkflow);
		}
		int previousSize = 0;
		while (previousSize != workflow.size()) {
			this.assignActions(workflow, actionTimeLookup, assignedWorkflowMap, new HashSet<Workflow.Node>(), this.maxDepth);
			
			previousSize = 0;
			for (Assignment assignedWorkflow : assignedWorkflowMap.values()) {
				previousSize += assignedWorkflow.size();
			}
		}
		
		return new ArrayList<Assignment>(assignedWorkflowMap.values());
	}
	
	private double assignActions(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			Map<String, Assignment> assignments, Set<Workflow.Node> visitedNodes, int depth ) {
		if (visitedNodes.size() == workflow.size()) {
			return new BufferedAssignments(assignments.values()).time();
		}
		if (depth == 0) {
			GreedyScheduler greedyScheduler = new GreedyScheduler();
			List<Assignment> assignedWorkflows = new ArrayList<Assignment>(assignments.values());
			BufferedAssignments buffered = new BufferedAssignments(assignedWorkflows);
			greedyScheduler.finishSchedule(workflow, actionTimeLookup, assignedWorkflows, buffered, visitedNodes);
			return buffered.time();
		}
		for (Assignment assignedWorkflow : assignments.values()) {
			for (ActionTime actionTime : assignedWorkflow) {
				visitedNodes.add(actionTime.getNode());
			}
		}
		
		List<Workflow.Node> availableNodes = workflow.getAvailableNodes(visitedNodes); 
		
		double bestTime = Double.MAX_VALUE;
		double bestActionTime = 0.0;
		Workflow.Node bestNode = null;
		String bestAgent = "";
		
		for (Workflow.Node node : availableNodes) {
			
			Set<Workflow.Node> futureVisitedNodes = new HashSet<Workflow.Node>(visitedNodes);
			futureVisitedNodes.add(node);
			
			for (Map.Entry<String, Assignment> entry : assignments.entrySet()) {
				Map<String, Assignment> copied = SchedulingHelper.copyMap(assignments);
				String agent = entry.getKey();
				double time = actionTimeLookup.get(node.getAction());
				copied.get(agent).add(node, time);
				double sequenceTime = this.assignActions(workflow, actionTimeLookup, copied, futureVisitedNodes, depth - 1);
				if (sequenceTime < bestTime) {
					bestTime = sequenceTime;
					bestActionTime = time;
					bestNode = node;
					bestAgent = agent;
				}
			}
			
		}
		assignments.get(bestAgent).add(bestNode, bestActionTime);
		return bestTime;
	}
	
	

}
