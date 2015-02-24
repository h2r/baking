package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private final Comparator comparator;
	public TercioNode(Subtask node, String agent, double hA, double hR, double hP, double hD) {
		this.node = node;
		this.agent = agent;
		this.hA = hA;
		this.hR = hR;
		this.hP = hP;
		this.hD = hD;
		this.comparator = new TercioComparator();
	}
	
	@Override
	public String toString() {
		return this.agent + ", " + this.node.toString() + ", hA: " + this.hA + " hP: " + this.hP + " hR: " + this.hR + " hD: " + this.hD;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TercioNode)) {
			return false;
		}
		TercioNode tNode = (TercioNode)other;
		if (!this.node.equals(tNode.node)) {
			return false;
		}
		if (!this.agent.equals(tNode.agent)) {
			return false;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.node, this.agent);
	}
	public String getAgent() {
		return this.agent;
	}
	
	public Subtask getSubtask() {
		return this.node;
	}
	
	public static double computeP(Subtask subtask, String agent, Assignments assignments) {
		
		double sum = 0.0;
		Queue<Subtask> queue = new LinkedList<Subtask>(subtask.getChildren());
		while (queue.peek() != null) {
			Subtask child = queue.poll();
			if (!assignments.isAssigned(child, agent)) {
				sum += 1.0;
			}
			queue.addAll(child.getChildren());
		}
		
		
		return sum;
	}
	
	public static double computeR(Subtask subtask, Collection<Subtask> unexecuted) {
		double sum = 0.0;
		for (Subtask unexec : unexecuted) {
			if (subtask.resourceConflicts(unexec)) {
				sum += 1.0;
			}
		}
		return sum;
	}
	
	public static double computeA(String agent, Map<String, Set<Subtask>> available) {
		double sum = 0.0;
		double thisAgent = 0.0;
		for (Map.Entry<String, Set<Subtask>> entry : available.entrySet()) {
			sum += entry.getValue().size();
			if (entry.getKey().equals(agent)) {
				thisAgent = entry.getValue().size();
			}
		}
		return (sum == 0.0) ? 0.0 : 1.0 - thisAgent / sum;
	}
	
	public static double computeD() {
		return 1.0;
	}
	
	
	
	
	
	public static class TercioComparator implements Comparator<TercioNode> {
		public int compare(TercioNode o1, TercioNode o2) {
			int res = Double.compare(o1.hA, o2.hA);
			if (res != 0) {
				return res;
			}
			res = Double.compare(o1.hP, o2.hP);
			if (res != 0) {
				return res;
			}
			res = Double.compare(o1.hR, o2.hR);
			if (res != 0) {
				return res;
			}
			return Double.compare(o1.hD, o2.hD);
		}
		
	}

}
