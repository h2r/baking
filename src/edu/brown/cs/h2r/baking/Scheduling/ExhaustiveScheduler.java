package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;
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
	public List<AssignedWorkflow> schedule(Workflow workflow,
			Map<String, Map<Node, Double>> actionTimeLookup) {
		Map<String, AssignedWorkflow> assignedWorkflows = new HashMap<String, AssignedWorkflow>();
		List<String> agents = new ArrayList<String>(actionTimeLookup.keySet());
		
		for (String agent : agents) {
			AssignedWorkflow assignedWorkflow = new AssignedWorkflow(agent);
			assignedWorkflows.put(agent, assignedWorkflow);
		}
		int previousSize = 0;
		while (previousSize != workflow.size()) {
			this.assignActions(workflow, actionTimeLookup, assignedWorkflows, new HashSet<Workflow.Node>(), this.maxDepth);
			
			previousSize = 0;
			for (AssignedWorkflow assignedWorkflow : assignedWorkflows.values()) {
				previousSize += assignedWorkflow.size();
			}
		}
		
		return new ArrayList<AssignedWorkflow>(assignedWorkflows.values());
	}
	
	private double assignActions(Workflow workflow, Map<String, Map<Node, Double>> actionTimeLookup, 
			Map<String, AssignedWorkflow> assignments, Set<Workflow.Node> visitedNodes, int depth ) {
		if (visitedNodes.size() == workflow.size()) {
			return SchedulingHelper.computeSequenceTime(new ArrayList<AssignedWorkflow>(assignments.values()));
		}
		if (depth == 0) {
			GreedyScheduler greedyScheduler = new GreedyScheduler();
			List<AssignedWorkflow> assignedWorkflows = new ArrayList<AssignedWorkflow>(assignments.values());
			greedyScheduler.schedule(workflow, actionTimeLookup, assignedWorkflows, visitedNodes);
			return SchedulingHelper.computeSequenceTime(assignedWorkflows);
		}
		for (AssignedWorkflow assignedWorkflow : assignments.values()) {
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
			
			for (Map.Entry<String, AssignedWorkflow> entry : assignments.entrySet()) {
				Map<String, AssignedWorkflow> copied = SchedulingHelper.copyMap(assignments);
				String agent = entry.getKey();
				double time = actionTimeLookup.get(agent).get(node);
				copied.get(agent).addAction(node, time);
				double sequenceTime = this.assignActions(workflow, actionTimeLookup, copied, futureVisitedNodes, depth - 1);
				if (sequenceTime < bestTime) {
					bestTime = sequenceTime;
					bestActionTime = time;
					bestNode = node;
					bestAgent = agent;
				}
			}
			
		}
		assignments.get(bestAgent).addAction(bestNode, bestActionTime);
		return bestTime;
	}
	
	

}
