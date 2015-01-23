package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;

public abstract class HeuristicScheduler implements Scheduler {
	protected final boolean useActualValues;
	public HeuristicScheduler(boolean useActualValues) {
		this.useActualValues = useActualValues;
	}

	@Override
	public List<Assignment> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		Map<String, Assignment> assignedWorkflows = new HashMap<String, Assignment>();
		for (String agent : agents) {
			Assignment assignedWorkflow = new Assignment(agent, actionTimeLookup, this.useActualValues);
			assignedWorkflows.put(agent, assignedWorkflow);
		}
		
		this.assignActions(workflow, actionTimeLookup, assignedWorkflows, agents);
		return new ArrayList<Assignment>(assignedWorkflows.values());
	}

	private void assignActions(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			Map<String, Assignment> assignments, List<String> agents) {
		
		Set<Workflow.Node> visitedNodes  = new HashSet<Workflow.Node>();
		while (visitedNodes.size() != workflow.size()) {
		
			List<Workflow.Node> availableNodes = workflow.getAvailableNodes(visitedNodes); 
			
			
			Map<Workflow.Node, Map<String, Double>> timeMap = 
					this.buildTimeMap(agents, actionTimeLookup, availableNodes);
			
			Map<Workflow.Node, Map<String, Double>> heuristics = this.getWeights(timeMap, assignments);
			
			this.addBestAction(actionTimeLookup, assignments, visitedNodes,
					heuristics);
		}
	}

	private void addBestAction(ActionTimeGenerator actionTimeLookup,
			Map<String, Assignment> assignments,
			Set<Workflow.Node> visitedNodes,
			Map<Workflow.Node, Map<String, Double>> heuristics) {
		
		String bestAgent = "";
		Double bestWeight = -Double.MAX_VALUE;
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
					GroundedAction ga = node.getAction();
					ga.params[0] = agent;
				}
			}
		}
		
		assignments.get(bestAgent).add(bestAction);
		visitedNodes.add(bestAction);
	}

	private Map<Workflow.Node, Map<String, Double>> buildTimeMap(List<String> agents, ActionTimeGenerator actionTimeLookup,
			List<Workflow.Node> availableNodes) {
		
		Map<Workflow.Node, Map<String, Double>> heuristicMap = new HashMap<Workflow.Node, Map<String, Double>>();
		
		for (Workflow.Node node : availableNodes) {
			Map<String, Double> agentMap = new HashMap<String, Double>();
			heuristicMap.put(node, agentMap);
			for (String agent : agents) {
				GroundedAction ga = node.getAction();
				ga.params[0] = agent;
				Double time = actionTimeLookup.get(ga, false);
				agentMap.put(agent, time);
			}
		}
		return heuristicMap;
	}
	

	
	protected Map<String, Assignment> getBuffered(List<Assignment> assignments) {
		Map<String, Assignment> buffered = new HashMap<String, Assignment>();
		for (Assignment workflow : assignments) {
			buffered.put(workflow.getId(), workflow);
		}
		return buffered;
	}
	
	protected abstract Map<Workflow.Node, Map<String, Double>> getWeights(Map<Workflow.Node, Map<String, Double>> times, 
			Map<String, Assignment> assignments);
}
