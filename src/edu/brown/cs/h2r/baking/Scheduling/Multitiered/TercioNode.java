package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;

public class TercioNode {
	private final Subtask node;
	private final String agent;
	private final double hA;
	private final double hR;
	private final double hP;
	private final double hD;
	public TercioNode(Subtask node, String agent, double hA, double hR, double hP, double hD) {
		this.node = node;
		this.agent = agent;
		this.hA = hA;
		this.hR = hR;
		this.hP = hP;
		this.hD = hD;
	}
	
	@Override
	public String toString() {
		return this.agent + ", " + this.node.toString() + ", hA: " + this.hA + " hP: " + this.hP + " hR: " + this.hR + " hD: " + this.hD;
	}
	
	public String getAgent() {
		return this.agent;
	}
	
	public Subtask getNode() {
		return this.node;
	}
	
	public static double computeA(Collection<Subtask> availableAssignedNodes ) {
		int size = availableAssignedNodes.size();
		return (size == 0) ? 2.0 : 1.0 / size;
	}
	
	public static double computeR(Assignments assignments, Set<String> nodeResources) {
		Map<String, Integer> resourceCounts = new HashMap<String, Integer>();
		for (AgentAssignment assignment : assignments.getAssignments()) {
			for (Subtask node : assignment.nodes()) {
				Set<String> resources = node.getResources();
				for (String resource : resources) {
					Integer count = resourceCounts.get(resource);
					if (count == null) {
						resourceCounts.put(resource, 1);
					} else {
						resourceCounts.put(resource, count+1);
					}
					
				}
			}
		}
		
		double sum = 0;
		for (String resource : nodeResources) {
			Integer count =  resourceCounts.get(resource);
			sum += (count == null) ? 0 : count;
		}
		return sum;
	}
	
	public static double computeP(String thisAgent, Subtask thisNode, Assignments assignments, Set<Subtask> visited ) {
		double sum = 0.0;
		for (AgentAssignment assignment : assignments.getAssignments()) {
			if (assignment.getId().equals(thisAgent)) {
				continue;
			}
			
			for (AssignedSubtask subtask : assignment) {
				Subtask node = subtask.getSubtask();
				if (!visited.contains(node)) {
					if (thisNode.isAncestorOf(node)) {
						sum++;
					}
				}
			}
		}
		
		return sum;
	}
	
	public static double computeD() {
		return 1.0;
	}
	
	
	
	
	
	public static class TercioComparator implements Comparator<TercioNode> {
		public int compare(TercioNode o1, TercioNode o2) {
			if (o1.hA != o2.hA) {
				return Double.compare(o1.hA, o2.hA);
			}
			if (o1.hP != o2.hP) {
				return Double.compare(o1.hP, o2.hP);
			}
			if (o1.hD != o2.hD) {
				return -Double.compare(o1.hD, o2.hD);
			}
			if (o1.hR != o2.hR) {
				return Double.compare(o1.hR, o2.hR);
			}
			return 0;
		}
		
	}

}
