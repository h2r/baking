package edu.brown.cs.h2r.baking.Scheduling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;

public class HeuristicSearchSequencer {
	private final boolean rearrangeOrder;
	public HeuristicSearchSequencer(boolean rearrangeOrder) {
		this.rearrangeOrder = rearrangeOrder;
	}
	public BufferedAssignments sequenceAssignments(List<Assignment> assignments) {
		HashIndexedHeap<SequenceNode> openQueue = new HashIndexedHeap<SequenceNode>(new SequenceNode.SequenceComparator());
		SequenceNode first = new SequenceNode(assignments, this.rearrangeOrder);
		openQueue.insert(first);
		
		while(openQueue.peek() != null) {
			SequenceNode next = openQueue.poll();
			
			if (next.complete()) {
				return next.getCompletedBuffered();
			}
			Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
			for (Assignment assignment : next.getBufferedAssignments().getAssignmentMap().values()) {
				visited.addAll(assignment.nodes());
			}
			Map<String, List<Workflow.Node>> available = new HashMap<String, List<Workflow.Node>>();
			for (Assignment assignment : assignments) {
				List<Workflow.Node> assignmentAvailable = assignment.nodes(visited);
				assignmentAvailable.removeAll(visited);
				available.put(assignment.getId(), assignmentAvailable);
			}
			
			for (Map.Entry<String, List<Workflow.Node>> entry : available.entrySet()) {
				String agent = entry.getKey();
				for (Workflow.Node node : entry.getValue()) {
					SequenceNode newNode = SequenceNode.add(next, node, agent);
					
					if (newNode == null) {
						newNode = SequenceNode.addAndRearrange(next, node, agent);
					}
					
					if (newNode != null && openQueue.containsInstance(newNode) == null) {
						openQueue.insert(newNode);
					}
				}
			}			
		}
		throw new RuntimeException("Could not complete sequencing");
	}

}
