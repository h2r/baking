package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public abstract class HeuristicScheduler implements Scheduler {

	@Override
	public List<AssignedWorkflow> schedule(Workflow workflow,
			Map<String, Map<Node, Double>> actionTimeLookup) {
		Map<String, AssignedWorkflow> assignedWorkflows = new HashMap<String, AssignedWorkflow>();
		List<String> agents = new ArrayList<String>(actionTimeLookup.keySet());
		for (String agent : agents) {
			AssignedWorkflow assignedWorkflow = new AssignedWorkflow(agent);
			assignedWorkflows.put(agent, assignedWorkflow);
		}
		
		boolean keepGoing = true;
		int previousSize = 0;
		this.assignActions(workflow, actionTimeLookup, assignedWorkflows);
		return new ArrayList<AssignedWorkflow>(assignedWorkflows.values());
	}

	private void assignActions(Workflow workflow, Map<String, Map<Node, Double>> actionTimeLookup, 
			Map<String, AssignedWorkflow> assignments) {
		
		Set<Workflow.Node> visitedNodes  = new HashSet<Workflow.Node>();
		while (visitedNodes.size() != workflow.size()) {
		
			List<Workflow.Node> availableNodes = workflow.getAvailableNodes(visitedNodes); 
			
			
			Map<Workflow.Node, Map<String, Double>> timeMap = 
					this.buildTimeMap(actionTimeLookup, availableNodes);
			
			Map<Workflow.Node, Map<String, Double>> heuristics = this.getWeights(timeMap, assignments);
			
			this.addBestAction(actionTimeLookup, assignments, visitedNodes,
					heuristics);
		}
	}

	private void addBestAction(Map<String, Map<Node, Double>> actionTimeLookup,
			Map<String, AssignedWorkflow> assignments,
			Set<Workflow.Node> visitedNodes,
			Map<Workflow.Node, Map<String, Double>> heuristics) {
		
		String bestAgent = "";
		Double bestWeight = -Double.MAX_VALUE;
		double bestTime = 0.0;
		Workflow.Node bestAction = null;
		
		for (Map.Entry<Workflow.Node, Map<String, Double>> entry : heuristics.entrySet()) {
			Workflow.Node node = entry.getKey();
			Map<String, Double> agentMap = entry.getValue();
			
			for (Map.Entry<String, Double> entry2 : agentMap.entrySet()) {
				String agent = entry2.getKey();
				Double weight = entry2.getValue();
				if (weight > bestWeight) {
					bestAgent = agent;
					bestWeight = weight;
					bestAction = node;
					bestTime = actionTimeLookup.get(agent).get(node);
				}
			}
		}
		
		assignments.get(bestAgent).addAction(bestAction, bestTime);
		visitedNodes.add(bestAction);
	}

	private Map<Workflow.Node, Map<String, Double>> buildTimeMap(Map<String, Map<Node, Double>> actionTimeLookup,
			List<Workflow.Node> availableNodes) {
		
		Map<Workflow.Node, Map<String, Double>> heuristicMap = new HashMap<Workflow.Node, Map<String, Double>>();
		
		for (Workflow.Node node : availableNodes) {
			Map<String, Double> agentMap = new HashMap<String, Double>();
			heuristicMap.put(node, agentMap);
			for (Map.Entry<String, Map<Node, Double>> entry : actionTimeLookup.entrySet()) {
				Double time = entry.getValue().get(node);
				agentMap.put(entry.getKey(), time);
			}
		}
		return heuristicMap;
	}
	

	
	protected Map<String, AssignedWorkflow> getBuffered(List<AssignedWorkflow> assignments) {
		Map<String, AssignedWorkflow> buffered = new HashMap<String, AssignedWorkflow>();
		for (AssignedWorkflow workflow : assignments) {
			buffered.put(workflow.getId(), workflow);
		}
		return buffered;
	}
	
	protected abstract Map<Workflow.Node, Map<String, Double>> getWeights(Map<Workflow.Node, Map<String, Double>> times, 
			Map<String, AssignedWorkflow> assignments);
}
