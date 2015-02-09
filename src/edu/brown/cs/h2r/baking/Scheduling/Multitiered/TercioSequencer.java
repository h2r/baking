package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class TercioSequencer implements Sequencer {
	private final boolean useActualValues;
	public TercioSequencer(boolean useActualValues) {
		this.useActualValues = useActualValues;
	}
	
	@Override
	public Assignments sequence(Assignments assignments, ActionTimeGenerator timeGenerator) {
		Assignments sequenced = new Assignments(assignments.getAgents(), timeGenerator, this.useActualValues);
		HashIndexedHeap<TercioNode> priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator());
		
		double currentTime = 0.0;
		Set<Subtask> visited = new HashSet<Subtask>();
		Set<Subtask> assigned = new HashSet<Subtask>(); 
		Map<Subtask, Double> sequencedTimes = new HashMap<Subtask, Double>();
		boolean keepGoing = true;
		
		while (keepGoing) {
			for (AgentAssignment assignment : sequenced.getAssignments()) {
				assignment.waitUntil(currentTime);
			}
			keepGoing = false;
			priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator());
			Map<String, List<Subtask>> availableNodes = new HashMap<String, List<Subtask>>();
			for (AgentAssignment assignment :  assignments.getAssignments()) {
				String agent = assignment.getId();
				List<Subtask> nodes = new ArrayList<Subtask>();
				for (Subtask node : assignment.availableSubtasks(visited)) {
					if (!assigned.contains(node)) {
						nodes.add(node);
					}
				}
				availableNodes.put(agent, nodes);
			}
			
			for (Map.Entry<String, List<Subtask>> entry : availableNodes.entrySet()) {
				String agent = entry.getKey();
				List<Subtask> nodes = entry.getValue(); 
				for (Subtask node : nodes) {
					
					double hA = TercioNode.computeA(nodes);
					double hR = TercioNode.computeR(assignments, node.getResources());
					double hP = TercioNode.computeP(agent, node, assignments, visited);
					double hD = 0.0;
					TercioNode tNode = new TercioNode(node, agent, hA, hR, hP, hD);
					priorityQueue.insert(tNode);
				}
			}
			
			int numAdded = 0;
			for (TercioNode tNode : priorityQueue) {
				Subtask node = tNode.getNode();
				String agent = tNode.getAgent();
				GroundedAction action = node.getAction(agent);
				double actionDuration = timeGenerator.get(action, this.useActualValues);
				if (this.add(sequenced, node, agent, currentTime, actionDuration)) {
					numAdded++;
					assigned.add(node);
					sequencedTimes.put(node, currentTime + actionDuration);
				}
			}
			
			if (assignments.allNodesAssigned(assigned)) {
				return sequenced;
			}
			
			// TODO 	Need to add in lowerbound so that the nexttime can wait
			double nextTime = Double.MAX_VALUE;
			for (AgentAssignment assignment : sequenced.getAssignments()) {
				Double time = assignment.nextTime(currentTime);
				if (time != null && time > currentTime && time < nextTime) {
					nextTime = time;
				}
			}
			if (nextTime == Double.MAX_VALUE) {
				List<Subtask> nextSubtasks = new ArrayList<Subtask>();
				
				for (AgentAssignment assignment :  assignments.getAssignments()) {
					String agent = assignment.getId();
					List<Subtask> nodes = new ArrayList<Subtask>();
					for (Subtask node : assignment.availableSubtasks(visited)) {
						if (!assigned.contains(node)) {
							nextSubtasks.add(node);
						}
					}
				}
				
				for (Subtask subtask : nextSubtasks) {
					double latestLowerbound = 0.0;
					boolean allTasksAssigned = true;
					for (TemporalConstraint constraint : subtask.getConstraints()) {
						if (assigned.contains(constraint.subtask)) {
							Double taskTime = sequencedTimes.get(constraint.subtask);
							latestLowerbound = Math.max(latestLowerbound, taskTime + constraint.lowerBound);
						} else {
							allTasksAssigned = false;
						}
					}
					if (allTasksAssigned && latestLowerbound > currentTime) {
						nextTime = Math.min(nextTime, latestLowerbound);
					}
				}
			}
			if (numAdded == 0 && (currentTime == nextTime)) {
				System.err.println("Sequencing failed");
				return null;
			}
			
			
			keepGoing = (currentTime != nextTime);
			
			currentTime = nextTime;
			for (AgentAssignment assignment : sequenced.getAssignments()) {
				keepGoing |= visited.addAll(assignment.completedSubtasks(currentTime));
			}
		}
		return sequenced;
	}
	
	private boolean add(Assignments assignments, Subtask task, String agent, double currentTime, double actionDuration) {
		if (assignments.isAssigned(task, agent)) {
			return true;
		}
		
		if (!assignments.isSubtaskAvailable(task, agent, currentTime, actionDuration)) {
			return false;
		}
		
		return assignments.add(task, agent);
	}
	
	

}
