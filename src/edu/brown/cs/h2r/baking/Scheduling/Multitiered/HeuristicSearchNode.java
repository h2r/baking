package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.Comparator;

public class HeuristicSearchNode {
	private final Assignments assignments;
	private final Assignments sequencedAssignments;
	private final Workflow workflow;
	private final double time;
	public HeuristicSearchNode(Workflow workflow, Assignments assignments, Assignments sequenced) {
		this.assignments = assignments;
		this.sequencedAssignments = sequenced;
		this.time = this.sequencedAssignments.time();
		this.workflow = workflow;
	}
	
	public double time() {
		return this.time;
	}
	
	public Assignments getCurrentAssignments() {
		return this.assignments;
	}
	
	public Assignments getSequencedAssignments() {
		return this.sequencedAssignments;
	}
	

	public boolean complete() {
		return this.assignments.allNodesAssigned(workflow.getSubtasks());
	}
	
	
	public static class HeuristicSearchNodeComparator implements Comparator<HeuristicSearchNode> {
		@Override
		public int compare(HeuristicSearchNode o1, HeuristicSearchNode o2) {
			return -Double.compare(o1.time(), o2.time());
		}
	}


}
