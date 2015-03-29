package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;

public class TercioSequencer extends Sequencer {

	public TercioSequencer(boolean useActualValues) {
		super(useActualValues);
	}

	@Override
	public Assignments finishSequence(Assignments assignments, Assignments sequenced, ActionTimeGenerator timeGenerator) {
		HashIndexedHeap<TercioNode> priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator());
		double currentTime = 0.0;
		Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
		Set<Workflow.Node> assigned = new HashSet<Workflow.Node>(); 
		boolean keepGoing = true;
		while (keepGoing) {
			for (Assignment assignment : assignments) {
				assignment.waitUntil(currentTime);
			}
			keepGoing = false;
			priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator());
			Map<String, List<Workflow.Node>> availableNodes = new HashMap<String, List<Workflow.Node>>();
			for (Assignment assignment : assignments) {
				String agent = assignment.getId();
				List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
				for (Workflow.Node node : assignment.nodes(visited)) {
					if (!assigned.contains(node)) {
						nodes.add(node);
					}
				}
				availableNodes.put(agent, nodes);
			}
			
			for (Map.Entry<String, List<Workflow.Node>> entry : availableNodes.entrySet()) {
				String agent = entry.getKey();
				List<Workflow.Node> nodes = entry.getValue(); 
				for (Workflow.Node node : nodes) {
					
					double hA = TercioNode.computeA(nodes);
					double hR = TercioNode.computeR(assignments, node.getResources());
					double hP = TercioNode.computeP(agent, node, assignments, visited);
					double hD = 0.0;
					TercioNode tNode = new TercioNode(node, agent, hA, hR, hP, hD);
					priorityQueue.insert(tNode);
				}
			}
			
			for (TercioNode tNode : priorityQueue) {
				Workflow.Node node = tNode.getNode();
				String agent = tNode.getAgent();
				if (sequenced.add(node, agent)) {
					assigned.add(node);
				}
			}
			
			double nextTime = Double.MAX_VALUE;
			for (Assignment assignment : assignments) {
				Double time = assignment.nextTime(currentTime);
				if (time != null && time > currentTime && time < nextTime) {
					nextTime = time;
				}
			}
			if (nextTime == Double.MAX_VALUE) {
				break;
			}
			keepGoing = (currentTime != nextTime);
			
			currentTime = nextTime;
			for (Assignment assignment : assignments) {
				keepGoing |= visited.addAll(assignment.nodes(currentTime));
			}
		}
		return sequenced;
	}

	@Override
	public String getDescription() {
		return this.getClass().getSimpleName();
	}
	
	
	/*
	public boolean shrinkAssignments(Assignments sequenced) {
		// Do some pre-trimming
		for (Assignment assignment : sequenced) {
			assignment.trim();
		}
		
		
		boolean keepGoing = true;
		double currentTime = -1.0;
		boolean modified = false;
		while(keepGoing) {
			// List to keep track of assignments that are waiting at the current time
			List<Assignment> assignmentsWaiting = new ArrayList<Assignment>();
			double timeFirstWait = Double.MAX_VALUE;
			for (Assignment assignment : assignments) {
				for (int j = 0; j < assignment.size(); j++) {
					Workflow.Node node = assignment.nodes().get(j);
					double endTime = assignment.completionTimes().get(j);
					double startTime = endTime - assignment.times().get(j);
					
					// if the first node after the current time is a waiting node, and it's earlier than the previously found one
					// then that's the new timeFirstWait. If there are more than one, add them to the list
					if (node == null && startTime > currentTime && startTime <= timeFirstWait) {
						if (startTime < timeFirstWait) {
							timeFirstWait = startTime;
							assignmentsWaiting.clear();
						}						
						assignmentsWaiting.add(assignment);
					} else if (startTime > timeFirstWait){ // if we've gone past a previously found timeFirstWait then break
						break;
					}
				}
			}
			
			if (timeFirstWait == Double.MAX_VALUE) {
				return modified;
			}
			
			// For each assignment that's currently waiting, we need to see if we can bump up actions
			for (Assignment waiting : assignmentsWaiting) {
				Set<Workflow.Node> slice = new HashSet<Workflow.Node>();
				Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
				// get the position of the node after the first waiting (2 because 1 for completionTimes, and 1 for the nextnode
				int position = waiting.position(timeFirstWait);
				if (position < waiting.completionTimes().get(position)) {
					position++;
				} else {
					position += 2;
				}
				while (position < waiting.size() && waiting.nodes().get(position) == null) {
					position++;
				}
				if (position >= waiting.size()) {
					continue;
				}
				double newEndTime = waiting.time();
				// find the end time if we were to bump of this action
				if (position < waiting.size() - 1) {
					newEndTime = timeFirstWait + waiting.times().get(position);
				} else {
					continue;
				}
				
				// get the comppleted nodes at the timefirstwait, as well as the slice of nodes occuring during this action
				for (Assignment assignment : assignments) {
					slice.addAll(assignment.slice(timeFirstWait, newEndTime));
					List<Workflow.Node> list = assignment.nodes(0.0, timeFirstWait);
					if (list != null) {
						visited.addAll(list);
					}
				} 
				
				// check of this node is available at the new time, and it doesn't conflict with any others
				// if so, swap the wait node and node
				Workflow.Node nodeToSwitch = waiting.nodes().get(position);
				if (nodeToSwitch.isAvailable(visited) && !nodeToSwitch.resourceConflicts(slice)) {
					waiting.swap(position - 1, position);
					modified = true;
				}
				waiting.trim();
			}
			
			currentTime = timeFirstWait;
		}
		return modified;
	}*/

}
